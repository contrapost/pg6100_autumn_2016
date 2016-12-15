package org.pg6100.exam.gamerest;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pg6100.exam.gamecommands.dto.AnswerDto;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Created by arcuri82 on 05-Dec-16.
 */
public class GameRestTestBase {

    public static final String QUIZ_MOCKED_URL = "http://localhost:8099/quiz/api";

    private static WireMockServer wiremockServer;

    @BeforeClass
    public static void initRestAssured() {

        // RestAssured configs shared by all the tests
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/game/api";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

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
    public void testGetRandom() throws Exception{

        String id = "42";
        String json = "{\"id\":\""+id+"\"}";

        wiremockServer.stubFor(
                WireMock.get(WireMock.urlMatching(".*/quizzes/random.*"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withHeader("Content-Length", "" + json.getBytes("utf-8").length)
                        .withBody(json)
                )
        );

        given().accept(ContentType.JSON)
                .get("/random")
                .then()
                .statusCode(200)
                .body("quizId", is(id));
    }

    @Test
    public void testGetRandomFail() throws Exception{

        wiremockServer.resetAll();

        given().accept(ContentType.JSON)
                .get("/random")
                .then()
                .statusCode(500);
    }

    @Test
    public void testPlayGameQuizNotFound(){

        String quizId = "123456";
        int index = 0;

        AnswerDto answer = new AnswerDto(quizId, index);

        given().accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(answer)
                .post("/games")
                .then()
                .statusCode(400);
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

    @Test
    public void testPlayGameError() throws Exception{

        String quizId = "123456";
        int index = 0;

        stubGameRequest(500, quizId, 0);

        AnswerDto answer = new AnswerDto(quizId, index);

        given().accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(answer)
                .post("/games")
                .then()
                .statusCode(500);
    }

    @Test
    public void testPlayGameCorrect() throws Exception{

        String quizId = "123456";
        int index = 0;

        stubGameRequest(200, quizId, index);

        AnswerDto answer = new AnswerDto(quizId, index);

        given().accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(answer)
                .post("/games")
                .then()
                .statusCode(200)
                .body("isCorrectAnswer", is(true))
        ;
    }

    @Test
    public void testPlayGameWrong() throws Exception{

        String quizId = "123456";
        int index = 0;

        stubGameRequest(200, quizId, index+1);

        AnswerDto answer = new AnswerDto(quizId, index);

        given().accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(answer)
                .post("/games")
                .then()
                .statusCode(200)
                .body("isCorrectAnswer", is(false))
        ;
    }
}