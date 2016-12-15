package org.pg6100.exam.quizimp.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pg6100.exam.quizapi.dto.hal.HalLink;
import org.pg6100.exam.quizapi.dto.hal.ListDto;
import org.pg6100.exam.quizapi.QuizRest;
import org.pg6100.exam.quizapi.dto.CreateQuizDto;
import org.pg6100.exam.quizapi.dto.ReadQuizDto;
import org.pg6100.exam.quizimp.data.Quiz;
import org.pg6100.exam.quizimp.ejb.QuizEjb;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static org.pg6100.exam.quizimp.rest.EjbUtil.ejbCall;

/**
 * Created by arcuri82 on 02-Dec-16.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class QuizRestImp implements QuizRest {

    @Context
    private UriInfo uriInfo;

    @EJB
    private QuizEjb quizEjb;

    @Override
    public ListDto<ReadQuizDto> getQuizzes(Integer offset, Integer limit, String filter) {

        if (offset < 0) {
            throw new WebApplicationException("Negative offset: " + offset, 400);
        }

        if (limit < 1) {
            throw new WebApplicationException("Limit should be at least 1: " + limit, 400);
        }

        UriBuilder builder = uriInfo.getBaseUriBuilder()
                .path("/quizzes")
                .queryParam("limit", limit);

        List<Quiz> quizList;

        if (filter != null) {
            long subId;
            try {
                subId = Long.parseLong(filter);
            } catch (Exception e) {
                throw new WebApplicationException(400);
            }
            quizList = ejbCall(() -> quizEjb.getQuizzes(subId));

            builder.queryParam("filter", filter);

        } else {
            quizList = ejbCall(() -> quizEjb.getQuizzes());
        }

        ListDto<ReadQuizDto> dto = DtoTransformer.transform(quizList, offset, limit);

        dto._links.self = new HalLink(builder.clone()
                .queryParam("offset", offset)
                .build().toString()
        );

        if (!quizList.isEmpty() && offset > 0) {
            dto._links.previous = new HalLink(builder.clone()
                    .queryParam("offset", Math.max(offset - limit, 0))
                    .build().toString()
            );
        }
        if (offset + limit < quizList.size()) {
            dto._links.next = new HalLink(builder.clone()
                    .queryParam("offset", offset + limit)
                    .build().toString()
            );
        }

        return dto;
    }


    @Override
    public Response createQuiz(CreateQuizDto dto) {

        long subId;
        try {
            subId = Long.parseLong(dto.subCategoryId);
        } catch (Exception e) {
            throw new WebApplicationException(400);
        }

        long quizId = ejbCall(() -> quizEjb.createQuiz(
                subId,
                dto.question,
                dto.firstAnswer,
                dto.secondAnswer,
                dto.thirdAnswer,
                dto.fourthAnswer,
                dto.indexOfCorrectAnswer
        ));

        return Response.created(
                uriInfo.getBaseUriBuilder().path("/quizzes/" + quizId).build())
                .build();
    }

    @Override
    public ReadQuizDto getQuiz(long id) {

        Quiz quiz = verifyquizExist(id);

        return DtoTransformer.transform(quiz);
    }

    @Override
    public void deleteQuiz(long id) {
        verifyquizExist(id);
        ejbCall(() -> quizEjb.delete(id));
    }

    @Override
    public void updateQuiz(long id, String mergePatch) {

        ObjectMapper jackson = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = jackson.readValue(mergePatch, JsonNode.class);
        } catch (Exception e) {
            throw new WebApplicationException("Invalid JSON data as input: " + e.getMessage(), 400);
        }

        Quiz q = verifyquizExist(id);

        long subCategoryId = getNumber(q.getSubCategoryId(), jsonNode, "subCategoryId");
        String question = getText(q.getQuestion(), jsonNode, "question");
        String firstAnswer = getText(q.getFirstAnswer(), jsonNode, "firstAnswer");
        String secondAnswer = getText(q.getSecondAnswer(), jsonNode, "secondAnswer");
        String thirdAnswer = getText(q.getThirdAnswer(), jsonNode, "thirdAnswer");
        String fourthAnswer = getText(q.getFourthAnswer(), jsonNode, "fourthAnswer");
        int indexOfCorrectAnswer = (int) (long) getNumber(q.getIndexOfCorrectAnswer(), jsonNode, "indexOfCorrectAnswer");

        ejbCall(() -> quizEjb.updateQuiz(
                id, subCategoryId, question,
                firstAnswer, secondAnswer, thirdAnswer, fourthAnswer, indexOfCorrectAnswer
        ));
    }

    @Override
    public Response getQuizAtRandom() {

        long id = quizEjb.getRandomQuizId();

        return Response.status(307)
                .location(uriInfo.getBaseUriBuilder().path("/quizzes/" + id).build())
                .build();
    }


    //----------------------------------------------------------

    private Long getNumber(long defaultValue, JsonNode jsonNode, String varName) {
        if (jsonNode.has(varName)) {
            JsonNode nameNode = jsonNode.get(varName);
            if (nameNode.isNull()) {
                return 0l;
            } else if (nameNode.isNumber()) {
                return nameNode.asLong();
            } else {
                throw new WebApplicationException("Invalid JSON", 400);
            }
        } else {
            return defaultValue;
        }
    }

    private String getText(String defaultValue, JsonNode jsonNode, String varName) {
        if (jsonNode.has(varName)) {
            JsonNode nameNode = jsonNode.get(varName);
            if (nameNode.isNull()) {
                return null;
            } else if (nameNode.isTextual()) {
                return nameNode.asText();
            } else {
                throw new WebApplicationException("Invalid JSON", 400);
            }
        } else {
            return defaultValue;
        }
    }


    private Quiz verifyquizExist(long id) {

        Quiz quiz = ejbCall(() -> quizEjb.getQuiz(id));
        if (quiz == null) {
            throw new WebApplicationException(404);
        }
        return quiz;
    }
}
