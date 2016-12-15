package org.pg6100.exam.gamerest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.pg6100.exam.gamecommands.CheckIfCorrect;
import org.pg6100.exam.gamecommands.GetRandomQuiz;
import org.pg6100.exam.gamecommands.dto.AnswerDto;
import org.pg6100.exam.gamecommands.dto.GameQuizDto;
import org.pg6100.exam.gamecommands.dto.ResultDto;
import org.pg6100.exam.quizapi.dto.ReadQuizDto;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

/**
 * Created by arcuri82 on 02-Dec-16.
 */
@Api("Games")
@Path("/")
public class GameRest {

    public static final String QUIZ_URL_PROP = "quizUrl";

    private final UriBuilder base;

    public GameRest() {
        String quizUrl =  System.getProperty(QUIZ_URL_PROP, "http://someaddress");
        base = UriBuilder.fromUri(quizUrl);
    }

    @ApiOperation("Get a game with a random quiz")
    @Path("/random")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public GameQuizDto getRandomQuiz() {

        ReadQuizDto quizDto = new GetRandomQuiz(
                base.clone().path("/quizzes/random"))
                .execute();

        if (quizDto == null) {
            throw new WebApplicationException(500);
        }

        GameQuizDto gameDto = new GameQuizDto();
        gameDto.quizId = quizDto.id;
        gameDto.question = quizDto.question;
        gameDto.firstAnswer = quizDto.firstAnswer;
        gameDto.secondAnswer = quizDto.secondAnswer;
        gameDto.thirdAnswer = quizDto.thirdAnswer;
        gameDto.fourthAnswer = quizDto.fourthAnswer;

        return gameDto;
    }

    @ApiOperation("Answer a quiz")
    @Path("/games")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultDto playGame(AnswerDto answer) {

        if (answer.quizId == null || answer.chosenAnswerIndex == null
                || answer.chosenAnswerIndex < 0 || answer.chosenAnswerIndex > 3) {
            throw new WebApplicationException(400);
        }

        ResultDto resultDto = new ResultDto();
        resultDto.quizId = answer.quizId;
        resultDto.chosenAnswerIndex = answer.chosenAnswerIndex;

        CheckIfCorrect.QuizResult quizResult =
                new CheckIfCorrect(
                        base.clone().path("/quizzes"),
                        answer.quizId,
                        answer.chosenAnswerIndex)
                        .execute();

        switch (quizResult) {
            case ERROR:
                throw new WebApplicationException(500);
            case INVALID:
                throw new WebApplicationException(400);
            case CORRECT:
                resultDto.isCorrectAnswer = true;
                break;
            case WRONG:
                resultDto.isCorrectAnswer = false;
                break;
            default:
                throw new WebApplicationException(500);
        }

        return resultDto;
    }

}
