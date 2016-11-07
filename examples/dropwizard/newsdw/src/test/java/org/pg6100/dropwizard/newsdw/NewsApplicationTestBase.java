package org.pg6100.dropwizard.newsdw;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pg6100.dropwizard.newsdw.api.Formats;
import org.pg6100.dropwizard.newsdw.dto.NewsDto;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.delete;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;

/**
 * Created by arcuri82 on 11/7/2016.
 */
public abstract class NewsApplicationTestBase {

    @BeforeClass
    public static void initRestAssured() {

        // RestAssured configs shared by all the tests
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/newsdw/api/news";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }


    @Before
    @After
    public void clean() {

        List<NewsDto> list = Arrays.asList(given().accept(ContentType.JSON).get()
                .then()
                .statusCode(200)
                .extract().as(NewsDto[].class));

        list.stream().forEach(dto ->
                given().pathParam("id", dto.id)
                        .delete("/{id}")
                        .then().statusCode(204));

        get().then().statusCode(200).body("size()", is(0));
    }

     /*
        Following are just copy&paste
     */

    @Test
    public void testSwagger() {

        given().baseUri("http://localhost")
                .basePath("/newsdw/api")
                .port(8080)
                .accept(ContentType.JSON)
                .get("swagger.json")
                .then()
                .statusCode(200);
    }

    @Test
    public void testCreateAndGetWithNewFormat() {

        String author = "author";
        String text = "someText";
        String country = "Norway";
        NewsDto dto = new NewsDto(null, author, text, country, null);

        //no news
        given().contentType(Formats.V1_NEWS_JSON)
                .get()
                .then()
                .statusCode(200)
                .body("size()", is(0));

        //create a news
        String id = given().contentType(Formats.V1_NEWS_JSON)
                .body(dto)
                .post()
                .then()
                .statusCode(200)
                .extract().asString();

        //should be 1 news now
        given().contentType(Formats.V1_NEWS_JSON)
                .get()
                .then()
                .statusCode(200)
                .body("size()", is(1));

        //1 news with same data as the POST
        given().accept(Formats.V1_NEWS_JSON)
                .pathParam("id", id)
                .get("/{id}")
                .then()
                .statusCode(200)
                .body("newsId", is(id))
                .body("authorId", is(author))
                .body("text", is(text))
                .body("country", is(country));
    }

    @Test
    public void testDoubleDelete(){

        NewsDto dto = new NewsDto(null, "author", "text", "Norway", null);

        //create a news
        String id = given().contentType(Formats.V1_NEWS_JSON)
                .body(dto)
                .post()
                .then()
                .statusCode(200)
                .extract().asString();

        delete("/"+id).then().statusCode(204);

        //delete again
        delete("/"+id).then().statusCode(404); //note the change from 204 to 404
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

        delete("/id/" + id).then().statusCode(301); //instead of 204

        //get().then().body("id", not(contains(id)));
    }


    @Test
    public void testUpdate() throws Exception {

        String text = "someText";

        //first create with a POST
        String id = given().contentType(ContentType.JSON)
                .body(new NewsDto(null, "author", text, "Norway", null))
                .post()
                .then()
                .statusCode(200)
                .extract().asString();

        //check if POST was fine
        get("/id/" + id).then().body("text", is(text));

        String updatedText = "new updated text";

        //now change text with PUT
        given().contentType(ContentType.JSON)
                .pathParam("id", id)
                .body(new NewsDto(id, "foo", updatedText, "Norway", ZonedDateTime.now()))
                .put("/id/{id}")
                .then()
                .statusCode(301); // instead of 204

//        //was the PUT fine?
//        get("/id/" + id).then().body("text", is(updatedText));
//
//
//        //now rechange, but just the text
//        String anotherText = "yet another text";
//
//        given().contentType(ContentType.TEXT)
//                .body(anotherText)
//                .pathParam("id", id)
//                .put("/id/{id}/text")
//                .then()
//                .statusCode(204);
//
//        get("/id/" + id).then().body("text", is(anotherText));
    }

    @Test
    public void testMissingForUpdate() {

        given().contentType(ContentType.JSON)
                .body("{\"id\":-333}")
                .pathParam("id", "-333")
                .put("/id/{id}")
                .then()
                .statusCode(301); // instead of 404
    }

    @Test
    public void testUpdateNonMatchingId() {

        given().contentType(ContentType.JSON)
                .body(new NewsDto("222", "foo", "some text", "Norway", ZonedDateTime.now()))
                .pathParam("id", "-333")
                .put("/id/{id}")
                .then()
                .statusCode(301); //instead of 409
    }


    @Test
    public void testInvalidUpdate() {

        String id = given().contentType(ContentType.JSON)
                .body(new NewsDto(null, "author", "someText", "Norway", null))
                .post()
                .then()
                .extract().asString();

        String updatedText = "";

        given().contentType(ContentType.JSON)
                .pathParam("id", id)
                .body(new NewsDto(id, null, updatedText, null, null))
                .put("/id/{id}")
                .then()
                .statusCode(301); // instead of 400
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
    public void testInvalidGetByCountry() {

        get("/countries/foo").then().statusCode(400);
    }

    @Test
    public void testInvalidGetByCountryAndAuthor() {

        get("/countries/foo/authors/foo").then().statusCode(400);
    }


    @Test
    public void testInvalidAuthor() {

        given().contentType(ContentType.JSON)
                .body(new NewsDto(null, "", "text", "Norway", null))
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    public void testInvalidCountry() {

        given().contentType(ContentType.JSON)
                .body(new NewsDto(null, "author", "text", "foo", null))
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    public void testPostWithId() {
        given().contentType(ContentType.JSON)
                .body(new NewsDto("1", "author", "text", "Norway", null))
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    public void testPostWithWrongType() {

        /*
            HTTP Error 415: "Unsupported media type"
            The REST API is set to return data in JSon, ie
            @Produces(MediaType.APPLICATION_JSON)
            so, if ask for XML, we should get a 415 error.
            Note: a server might provide the same resource (on same URL)
            with different formats!
            Although nowadays most just deal with Json.
         */

        given().contentType(ContentType.XML)
                .body("<foo></foo>")
                .post()
                .then()
                .statusCode(415);
    }


    @Test
    public void testGetByInvalidId() {

        /*
            In this particular case, "foo" might be a valid id.
            however, as it is not in the database, and there is no mapping
            for a String id, the server will say "Not Found", ie 404.
         */

        get("/id/foo")
                .then()
                .statusCode(404);
    }
}
