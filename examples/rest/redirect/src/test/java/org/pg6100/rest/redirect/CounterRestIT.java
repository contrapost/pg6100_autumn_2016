package org.pg6100.rest.redirect;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pg6100.utils.web.JBossUtil;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;


public class CounterRestIT {


    @BeforeClass
    public static void initTestSuite() {
        JBossUtil.waitForJBoss(10);

        // RestAssured configs shared by all the tests
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/redirect/api/counters";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }


    @Before
    @After
    public void cleanCounters() {

        given().delete()
                .then()
                .statusCode(204);

        given().get()
                .then()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    public void testNoLatest() {

        given().get("latest")
                .then()
                .statusCode(409);
    }

    @Test
    public void testPermanentRedirect() {

        /*
            NOTE: by default, RestAssured will automatically
            follow the redirects
         */

        given().redirects().follow(false)
                .get("theLatest")
                .then()
                .statusCode(301)
                .header("Location", containsString("latest"));


        //automated re-direct to "latest", but now there is none
        given().accept(ContentType.JSON)
                .get("theLatest")
                .then()
                .statusCode(409);
    }


    @Test
    public void testFindLatest() {

        long id = createNew();

        String location = given()
                .redirects().follow(false)
                .get("latest")
                .then()
                .statusCode(307)
                .extract()
                .header("Location");

        given().accept(ContentType.JSON)
                .get(location)
                .then()
                .statusCode(200)
                .body("id", is(id));


        //with auto-redirect, equivalent to:
        given().accept(ContentType.JSON)
                .get("latest")
                .then()
                .statusCode(200)
                .body("id", is(id));
    }

    @Test
    public void testCreate2AndFindLatest() {

        long first = createNew();
        long second = createNew();

        String location = given()
                .redirects().follow(false)
                .get("latest")
                .then()
                .statusCode(307)
                .extract()
                .header("Location");

        given().accept(ContentType.JSON)
                .get(location)
                .then()
                .statusCode(200)
                .body("id", not(equalTo(first)))
                .body("id", is(second));
    }


    @Test
    public void testDoubleRedirection() {

        long id = createNew();

        String location = given()
                .redirects().follow(false)
                .get("theLatest")
                .then()
                .statusCode(301)
                .extract()
                .header("Location");

        location = given()
                .redirects().follow(false)
                .get(location)
                .then()
                .statusCode(307)
                .extract()
                .header("Location");

        given().accept(ContentType.JSON)
                .get(location)
                .then()
                .statusCode(200)
                .body("id", is(id));


        //the above is equivalent to:
        given().accept(ContentType.JSON)
                .get("theLatest")
                .then()
                .statusCode(200)
                .body("id", is(id));
    }


    private long createNew() {

        long id = System.currentTimeMillis();
        CounterDto dto = new CounterDto();
        dto.id = id;

        given().contentType(ContentType.JSON)
                .body(dto)
                .post()
                .then()
                .statusCode(201);

        return id;
    }
}