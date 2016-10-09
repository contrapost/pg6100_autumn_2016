package org.pg6100.rest.newsrest.api;

import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pg6100.rest.newsrest.dto.NewsDto;
import org.pg6100.utils.web.JBossUtil;


import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;

public class NewsRestIT {

    private static final Gson gson = new Gson();

    @BeforeClass
    public static void initClass() {
        JBossUtil.waitForJBoss(10);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/newsrest/api/news";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }


    @Before
    @After
    public void clean() {

        String body = given().accept(ContentType.JSON).get()
                .then()
                .statusCode(200)
                .extract().asString();
        List<NewsDto> list = Arrays.asList(gson.fromJson(body, NewsDto[].class));

        list.stream().forEach(dto ->
                given().pathParam("id", dto.id).delete("/id/{id}").then().statusCode(204));

        get().then().statusCode(200).body("size()", is(0));
    }

    @Test
    public void testCleanDB() {

        get().then()
                .statusCode(200)
                .body("size()", is(0));
    }


    @Test
    public void testCreateAndGet() {

        String author = "author";
        String text = "someText";
        String country = "Norway";
        NewsDto dto = new NewsDto(null, author, text, country, null);

        get().then().statusCode(200).body("size()", is(0));

        String id = given().contentType(ContentType.JSON)
                .body(dto)
                .post()
                .then()
                .statusCode(200)
                .extract().asString();

        get().then().statusCode(200).body("size()", is(1));

        given().pathParam("id", id)
                .get("/id/{id}")
                .then()
                .statusCode(200)
                .body("id", is(id))
                .body("authorId", is(author))
                .body("text", is(text))
                .body("country", is(country));
    }

    @Test
    public void testDelete() {

        String id = given().contentType(ContentType.JSON)
                .body(new NewsDto(null, "author", "text", "Norway", null))
                .post()
                .then()
                .statusCode(200)
                .extract().asString();

        get().then().body("id", contains(id));

        delete("/id/" + id);

        get().then().body("id", not(contains(id)));
    }


    @Test
    public void testUpdate() {

        String text = "someText";

        String id = given().contentType(ContentType.JSON)
                .body(new NewsDto(null, "author", text, "Norway", null))
                .post()
                .then()
                .statusCode(200)
                .extract().asString();

        get("/id/" + id).then().body("text", is(text));

        String updatedText = "new updated text";

        given().contentType(ContentType.JSON)
                .body(new NewsDto(id, null, updatedText, null, null))
                .put()
                .then()
                .statusCode(204);

        get("/id/" + id).then().body("text", is(updatedText));
    }


    private void createSomeNews() {
        createNews("a", "text", "Norway");
        createNews("a", "other text", "Norway");
        createNews("a", "more text", "Sweden");
        createNews("b", "text", "Norway");
        createNews("b", "yet another text", "Iceland");
        createNews("c", "text", "Iceland");
    }

    private void createNews(String authorId, String text, String country) {
        given().contentType(ContentType.JSON)
                .body(new NewsDto(null, authorId, text, country, null))
                .post()
                .then()
                .statusCode(200);
    }

    @Test
    public void testGetAll() {

        get().then().body("size()", is(0));
        createSomeNews();

        get().then().body("size()", is(6));
    }

    @Test
    public void testGetAllByCountry() {

        get().then().body("size()", is(0));
        createSomeNews();

        get("/countries/Norway").then().body("size()", is(3));
        get("/countries/Sweden").then().body("size()", is(1));
        get("/countries/Iceland").then().body("size()", is(2));
    }

    @Test
    public void testGetAllByAuthor() {

        get().then().body("size()", is(0));
        createSomeNews();

        get("/authors/a").then().body("size()", is(3));
        get("/authors/b").then().body("size()", is(2));
        get("/authors/c").then().body("size()", is(1));
    }

    @Test
    public void testGetAllByCountryAndAuthor() {

        get().then().body("size()", is(0));
        createSomeNews();

        get("/countries/Norway/authors/a").then().body("size()", is(2));
        get("/countries/Sweden/authors/a").then().body("size()", is(1));
        get("/countries/Iceland/authors/a").then().body("size()", is(0));
        get("/countries/Norway/authors/b").then().body("size()", is(1));
        get("/countries/Sweden/authors/b").then().body("size()", is(0));
        get("/countries/Iceland/authors/b").then().body("size()", is(1));
        get("/countries/Norway/authors/c").then().body("size()", is(0));
        get("/countries/Sweden/authors/c").then().body("size()", is(0));
        get("/countries/Iceland/authors/c").then().body("size()", is(1));
    }

    @Test
    public void testInvalidAuthor() {
//        try {
//            ejb.createNews("", "text", "Norway");
//        } catch (EJBException e) {
//            Throwable cause = Throwables.getRootCause(e);
//            assertTrue("Cause: " + cause, cause instanceof ConstraintViolationException);
//        }
    }

    @Test
    public void testInvalidText() {
//        try {
//            ejb.createNews("author", "", "Norway");
//        } catch (EJBException e) {
//            Throwable cause = Throwables.getRootCause(e);
//            assertTrue("Cause: " + cause, cause instanceof ConstraintViolationException);
//        }
    }

    @Test
    public void testInvalidCountry() {
//        try {
//            ejb.createNews("author", "text", "Foo");
//        } catch (EJBException e) {
//            Throwable cause = Throwables.getRootCause(e);
//            assertTrue("Cause: " + cause, cause instanceof ConstraintViolationException);
//        }
    }
}