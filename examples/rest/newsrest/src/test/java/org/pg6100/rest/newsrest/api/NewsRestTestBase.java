package org.pg6100.rest.newsrest.api;

import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.pg6100.rest.newsrest.dto.NewsDto;
import org.pg6100.utils.web.JBossUtil;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

public class NewsRestTestBase {

    private static final Gson gson = new Gson();

    @BeforeClass
    public static void initClass() {
        JBossUtil.waitForJBoss(10);

        // RestAssured configs shared by all the tests
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/newsrest/api/news";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }


    @Before
    @After
    public void clean() {

        /*
           Recall, as Wildfly is running as a separated process, changed
           in the database will impact all the tests.
           Here, we read each resource (GET), and then delete them
           one by one (DELETE)
         */
        String body = given().accept(ContentType.JSON).get()
                .then()
                .statusCode(200)
                .extract().asString();
        List<NewsDto> list = Arrays.asList(gson.fromJson(body, NewsDto[].class));


        /*
            Code 204: "No Content". The server has successfully processed the request,
            but the return HTTP response will have no body.
         */
        list.stream().forEach(dto ->
                given().pathParam("id", dto.id).delete("/id/{id}").then().statusCode(204));

        get().then().statusCode(200).body("size()", is(0));
    }
}
