package org.pg6100.exam.gamesoap;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pg6100.exam.gamesoap.client.AnswerDto;
import org.pg6100.exam.gamesoap.client.GameQuizDto;
import org.pg6100.exam.gamesoap.client.GameSoapImpService;
import org.pg6100.exam.gamesoap.client.ResultDto;
import org.pg6100.utils.web.JBossUtil;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.*;

/**
 * Created by arcuri82 on 06-Dec-16.
 */
public class GameSoapIT {

    private static org.pg6100.exam.gamesoap.client.GameSoap ws;

    private static WireMockServer wiremockServer;


    @BeforeClass
    public static void initClass() {

        JBossUtil.waitForJBoss(10);

        GameSoapImpService service = new GameSoapImpService();
        ws = service.getGameSoapImpPort();

        String url = "http://localhost:8080/gamesoap/GameSoapImp";

        ((BindingProvider) ws).getRequestContext().put(
                BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);

        wiremockServer = new WireMockServer(
                wireMockConfig().port(8099).notifier(new ConsoleNotifier(true))
        );
        wiremockServer.start();
    }

    @AfterClass
    public static void tearDown() {
        wiremockServer.stop();
    }

    @Before
    public void reset(){
        wiremockServer.resetAll();
    }

    @Test
    public void testGetRandom() throws Exception {

        String id = "42";
        String json = "{\"id\":\"" + id + "\"}";

        wiremockServer.stubFor(
                WireMock.get(WireMock.urlMatching(".*/quizzes/random.*"))
                        .willReturn(WireMock.aResponse()
                                .withHeader("Content-Type", "application/json; charset=utf-8")
                                .withHeader("Content-Length", "" + json.getBytes("utf-8").length)
                                .withBody(json)
                        )
        );

        GameQuizDto dto = ws.getRandomQuiz();
        assertEquals(id, dto.getQuizId());
    }

    @Test(expected = SOAPFaultException.class)
    public void testGetRandomFail() throws Exception{

        wiremockServer.resetAll();

        ws.getRandomQuiz();
    }

    @Test(expected = SOAPFaultException.class)
    public void testPlayGameQuizNotFound(){

        String quizId = "123456";
        int index = 0;

        AnswerDto answer = new AnswerDto();
        answer.setQuizId(quizId);
        answer.setChosenAnswerIndex(index);

        ws.playGame(answer);
    }

    private void stubGameRequest(int status, String quizId, int indexOfCorrectAnswer) throws Exception{

        String json = "{\"indexOfCorrectAnswer\": "+indexOfCorrectAnswer+"}";

        wiremockServer.stubFor(
                WireMock.get(WireMock.urlMatching(".*/quizzes/"+quizId))
                        .willReturn(WireMock.aResponse()
                                .withStatus(status)
                                .withHeader("Content-Type", "application/json; charset=utf-8")
                                .withHeader("Content-Length", "" + json.getBytes("utf-8").length)
                                .withBody(json)
                        )
        );

    }

    @Test(expected = SOAPFaultException.class)
    public void testPlayGameError() throws Exception{

        String quizId = "123456";
        int index = 0;

        stubGameRequest(500, quizId, 0);

        AnswerDto answer = new AnswerDto();
        answer.setQuizId(quizId);
        answer.setChosenAnswerIndex(index);

      ws.playGame(answer);
    }

    @Test
    public void testPlayGameCorrect() throws Exception{

        String quizId = "123456";
        int index = 0;

        stubGameRequest(200, quizId, index);

        AnswerDto answer = new AnswerDto();
        answer.setQuizId(quizId);
        answer.setChosenAnswerIndex(index);

        ResultDto dto = ws.playGame(answer);
        assertTrue(dto.isIsCorrectAnswer());
    }

    @Test
    public void testPlayGameWrong() throws Exception{

        String quizId = "123456";
        int index = 0;

        stubGameRequest(200, quizId, index+1);

        AnswerDto answer = new AnswerDto();
        answer.setQuizId(quizId);
        answer.setChosenAnswerIndex(index);

        ResultDto dto = ws.playGame(answer);
        assertFalse(dto.isIsCorrectAnswer());
    }

    @Test(expected = SOAPFaultException.class)
    public void testPlayGameInvalidIndex() throws Exception{

        String quizId = "123456";
        int index = -1;

        stubGameRequest(200, quizId, index);

        AnswerDto answer = new AnswerDto();
        answer.setQuizId(quizId);
        answer.setChosenAnswerIndex(index);

        ResultDto dto = ws.playGame(answer);
    }
}