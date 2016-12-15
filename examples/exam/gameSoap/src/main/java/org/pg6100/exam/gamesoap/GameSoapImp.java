package org.pg6100.exam.gamesoap;

import org.pg6100.exam.gamecommands.CheckIfCorrect;
import org.pg6100.exam.gamecommands.GetRandomQuiz;
import org.pg6100.exam.gamecommands.dto.AnswerDto;
import org.pg6100.exam.gamecommands.dto.GameQuizDto;
import org.pg6100.exam.gamecommands.dto.ResultDto;
import org.pg6100.exam.quizapi.dto.ReadQuizDto;

import javax.jws.WebService;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriBuilder;

/**
 * Created by arcuri82 on 05-Dec-16.
 */
@WebService(
        endpointInterface = "org.pg6100.exam.gamesoap.GameSoap"
)
public class GameSoapImp implements GameSoap {

    private final UriBuilder base;

    public static final String QUIZ_URL_PROP = "quizUrl";

    public GameSoapImp() {
        String quizUrl =  System.getProperty(QUIZ_URL_PROP, "http://someaddress");
        base = UriBuilder.fromUri(quizUrl);
    }

    @Override
    public GameQuizDto getRandomQuiz() {
        ReadQuizDto quizDto = new GetRandomQuiz(
                base.clone().path("/quizzes/random"))
                .execute();

        if (quizDto == null) {
            throw new RuntimeException("Internal failure. Failed to receive DTO from: "+base.toTemplate());
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

    @Override
    public ResultDto playGame(AnswerDto answer) {
        if (answer.quizId == null || answer.chosenAnswerIndex == null
                || answer.chosenAnswerIndex < 0 || answer.chosenAnswerIndex > 3) {
            throw new RuntimeException("Invalid parameters");
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
                throw new RuntimeException("Internal error");
            case INVALID:
                throw new RuntimeException("Invalid parameters");
            case CORRECT:
                resultDto.isCorrectAnswer = true;
                break;
            case WRONG:
                resultDto.isCorrectAnswer = false;
                break;
            default:
                throw new RuntimeException("Internal error");
        }

        return resultDto;
    }
}
