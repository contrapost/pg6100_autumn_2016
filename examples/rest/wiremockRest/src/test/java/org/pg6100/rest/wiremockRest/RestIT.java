package org.pg6100.rest.wiremockRest;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pg6100.utils.web.JBossUtil;

import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.client.WireMock;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created by arcuri82 on 11/2/2016.
 * <p>
 * Note: these tests only work if Wildfly is started with
 * <p>
 * -DfixerWebAddress=localhost:8099
 * <p>
 * in its JVM configuration
 */
public class RestIT {

    private static WireMockServer wiremockServer;

    @BeforeClass
    public static void initClass() {
        JBossUtil.waitForJBoss(10);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/wiremockRest/api/convert";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        /*
            WireMock will run as a process binding on port 8099 (in this case).
            We configure the application by, during testing, redirecting all
            calls to external services to WireMock instead.
            In WireMock, we need to define mocked answers.
         */
        wiremockServer = new WireMockServer(
                wireMockConfig().port(8099).notifier(new ConsoleNotifier(true))
        );
        wiremockServer.start();
    }

    @AfterClass
    public static void tearDown() {
        wiremockServer.stop();
    }


    private String getAMockedJsonResponse(double usd, double eur, double gbp) {
        String json = "{";
        json += "\"base\": \"NOK\" , ";
        json += "\"date\": \"2016-04-29\" , ";
        json += "\"rates\": {";
        json += "\"USD\": " + usd + ", ";
        json += "\"EUR\": " + eur + ", ";
        json += "\"GBP\": " + gbp + " ";
        json += "}";
        json += "}";
        return json;
    }

    private void stubJsonResponse(String json) throws Exception {

        /*
            here we are instructing WireMock to return the given json
            every time there is a request for

            /latest?base=NOK

         */

        wiremockServer.stubFor( //prepare a stubbed response for the given request
                WireMock.get( //define the GET request to mock
                        /*
                           recall regular expressions:
                           "." =  any character
                           "*" = zero or more times
                        */
                        urlMatching("/latest.*"))
                        .withQueryParam("base", WireMock.matching("NOK"))
                        // define the mocked response of the GET
                        .willReturn(WireMock.aResponse()
                                .withHeader("Content-Type", "application/json; charset=utf-8")
                                .withHeader("Content-Length", "" + json.getBytes("utf-8").length)
                                .withBody(json)));
    }

    @Test
    public void testEUR() throws Exception {

        double eur = 42;

        String json = getAMockedJsonResponse(1, eur, 2);
        stubJsonResponse(json);

        given().accept(ContentType.XML)
                .queryParam("from", "NOK")
                .queryParam("to", "EUR")
                .get()
                .then()
                .statusCode(200)
                .body("conversionDTO.rate", equalTo("" + eur));
    }

    @Test
    public void testUSD() throws Exception {

        double usd = 66;

        String json = getAMockedJsonResponse(usd, 1, 2);
        stubJsonResponse(json);

        given().accept(ContentType.XML)
                .queryParam("from", "NOK")
                .queryParam("to", "USD")
                .get()
                .then()
                .statusCode(200)
                .body("conversionDTO.rate", equalTo("" + usd));
    }

    @Test
    public void testGBP() throws Exception {

        double gbp = 99;

        String json = getAMockedJsonResponse(1, 2, gbp);
        stubJsonResponse(json);

        given().accept(ContentType.XML)
                .queryParam("from", "NOK")
                .queryParam("to", "GBP")
                .get()
                .then()
                .statusCode(200)
                .body("conversionDTO.rate", equalTo("" + gbp));
    }

}