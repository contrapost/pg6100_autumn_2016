package org.pg6100.workshop;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pg6100.utils.web.JBossUtil;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.*;

public class UserREST_IT {

    @BeforeClass
    public static void initClass(){
        JBossUtil.waitForJBoss(10);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/workshop/api/users";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }


    @Test
    public void testCreateAndGet(){

        String name = "foo";

        String location = given().contentType(ContentType.JSON)
                .body(new UserDto(name, null, null))
                .post()
                .then()
                .statusCode(201)
                .extract()
                .header("location");

        given().accept(ContentType.JSON)
                .get(location)
                .then()
                .statusCode(200)
                .body("name", is(name));
    }

    @Test
    public void testReplace(){

        String name = "foo";
        String surname = "bar";

        String location = given().contentType(ContentType.JSON)
                .body(new UserDto(name, null, null))
                .post()
                .then()
                .statusCode(201)
                .extract()
                .header("location");

        given().contentType(ContentType.JSON)
                .body(new UserDto(null, surname, null))
                .put(location)
                .then()
                .statusCode(204);

        given().accept(ContentType.JSON)
                .get(location)
                .then()
                .statusCode(200)
                .body("name", nullValue())
                .body("surname", is(surname));
    }

    @Test
    public void testUpdate(){

        String name = "foo";
        String surname = "bar";

        String location = given().contentType(ContentType.JSON)
                .body(new UserDto(name, null, null))
                .post()
                .then()
                .statusCode(201)
                .extract()
                .header("location");

        given().contentType("application/merge-patch+json")
                .body("{\"surname\":\""+surname+"\"}")
                .patch(location)
                .then()
                .statusCode(204);

        given().accept(ContentType.JSON)
                .get(location)
                .then()
                .statusCode(200)
                .body("name", is(name))
                .body("surname", is(surname));
    }
}