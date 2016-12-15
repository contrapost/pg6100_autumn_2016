package org.pg6100.exam.gamesoap;

import org.pg6100.exam.gamecommands.dto.AnswerDto;
import org.pg6100.exam.gamecommands.dto.GameQuizDto;
import org.pg6100.exam.gamecommands.dto.ResultDto;

import javax.jws.WebService;

/**
 * Created by arcuri82 on 05-Dec-16.
 */
@WebService(name = "GameSoap")
public interface GameSoap {

    GameQuizDto getRandomQuiz();

    ResultDto playGame(AnswerDto answer);
}
