package org.pg6100.exam.quizimp.rest;

import org.junit.Test;
import org.pg6100.exam.quizapi.Formats;
import org.pg6100.exam.quizapi.dto.CreateQuizDto;
import org.pg6100.exam.quizapi.dto.hal.ListDto;


import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

/**
 * Created by arcuri82 on 05-Dec-16.
 */
public class QuizRestIT extends TestsBase {


    private String createQuiz(String question) {

        String subId = createCtgAndSubAndGetIdOfSub("foo", "bar");

        String location = given().contentType(Formats.JSON_V1)
                .body(new CreateQuizDto(subId, question, "a0", "a1", "a2", "a3", 0))
                .post("/quizzes")
                .then()
                .statusCode(201)
                .extract().header("location");

        return location;
    }

    private int createSeveralQuizzes(int n) {

        String subId = createCtgAndSubAndGetIdOfSub("foo", "bar");

        for (int i = 0; i < n; i++) {
            given().contentType(Formats.JSON_V1)
                    .body(new CreateQuizDto(subId, "" + i, "a0", "a1", "a2", "a3", 0))
                    .post("/quizzes")
                    .then()
                    .statusCode(201);
        }

        return n;
    }

    private Set<String> getQuestions(ListDto<?> selfDto) {

        Set<String> values = new HashSet<>();
        selfDto.list.stream()
                .map(m -> (String) ((Map) m).get("question"))
                .forEach(t -> values.add(t));

        return values;
    }

    private void assertContainsTheSame(Collection<?> a, Collection<?> b) {
        assertEquals(a.size(), b.size());
        a.stream().forEach(v -> assertTrue(b.contains(v)));
        b.stream().forEach(v -> assertTrue(a.contains(v)));
    }

    @Test
    public void testCreateWithInvalidSubcategory() {

        given().contentType(Formats.JSON_V1)
                .body(new CreateQuizDto("5", "q", "a0", "a1", "a2", "a3", 0))
                .post("/quizzes")
                .then()
                .statusCode(400);
    }

    @Test
    public void testCreateAndGetQuiz() {

        String question = "Will this test pass?";
        String location = createQuiz(question);

        given().accept(Formats.JSON_V1)
                .get(location)
                .then()
                .statusCode(200)
                .body("question", is(question));
    }

    @Test
    public void testGetRandom() {

        String question = "How tricky is to test a non-deterministic method?";
        createQuiz(question);

        given().accept(Formats.JSON_V1)
                .get("/quizzes/random")
                .then()
                .statusCode(200)
                .body("question", is(question));
    }

    @Test
    public void testPatchChangeQuestion() {

        String question = "A question that will be changed";
        String location = createQuiz(question);

        String changed = question + ". Changed now";

        given().contentType(Formats.MERGE_PATCH_V1)
                .body("{\"question\":\"" + changed + "\"}")
                .patch(location)
                .then()
                .statusCode(204);

        given().accept(Formats.JSON_V1)
                .get(location)
                .then()
                .statusCode(200)
                .body("question", is(changed));
    }

    @Test
    public void testPatchRemoveQuestion() {

        String question = "A question that will be changed";
        String location = createQuiz(question);

        given().contentType(Formats.MERGE_PATCH_V1)
                .body("{\"question\":null}")
                .patch(location)
                .then()
                .statusCode(400);
    }

    private void changeSubId(String location, String subId, int statusCode){
        given().contentType(Formats.MERGE_PATCH_V1)
                .body("{\"subCategoryId\":" + subId + "}")
                .patch(location)
                .then()
                .statusCode(statusCode);
    }

    @Test
    public void testPatchInvalidSubId() {

        String location = createQuiz("A question");

        changeSubId(location, "1", 400);
        changeSubId(location, "\"-1\"", 400);
        //changeSubId(location, "null", 400); //this fails if id was 0
    }

    @Test
    public void testSelfLink() {

        int n = createSeveralQuizzes(30);
        int limit = 10;

        ListDto<?> listDto = given()
                .queryParam("limit", limit)
                .get("/quizzes")
                .then()
                .statusCode(200)
                .extract()
                .as(ListDto.class);

        assertEquals(n, (int) listDto.totalSize);
        assertEquals(0, (int) listDto.rangeMin);
        assertEquals(limit - 1, (int) listDto.rangeMax);

        assertNull(listDto._links.previous);
        assertNotNull(listDto._links.next);
        assertNotNull(listDto._links.self);

        //read again using self link
        ListDto<?> selfDto = given()
                .get(listDto._links.self.href)
                .then()
                .statusCode(200)
                .extract()
                .as(ListDto.class);

        Set<String> first = getQuestions(listDto);
        Set<String> self = getQuestions(selfDto);

        assertContainsTheSame(first, self);
    }

    @Test
    public void testNextLink() {

        int n = createSeveralQuizzes(30);
        int limit = 10;

        ListDto<?> listDto = given()
                .queryParam("limit", limit)
                .get("/quizzes")
                .then()
                .statusCode(200)
                .extract()
                .as(ListDto.class);

        assertEquals(n, (int) listDto.totalSize);
        assertNotNull(listDto._links.next.href);

        Set<String> values = getQuestions(listDto);
        String next = listDto._links.next.href;

        int counter = 0;

        //read pages until there is still a "next" link
        while (next != null) {

            counter++;

            int beforeNextSize = values.size();

            listDto = given()
                    .get(next)
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(ListDto.class);

            values.addAll(getQuestions(listDto));

            assertEquals(beforeNextSize + limit, values.size());
            assertEquals(counter * limit, (int) listDto.rangeMin);
            assertEquals(listDto.rangeMin + limit - 1, (int) listDto.rangeMax);

            if (listDto._links.next != null) {
                next = listDto._links.next.href;
            } else {
                next = null;
            }
        }

        assertEquals(n, values.size());
    }


    @Test
    public void textPreviousLink() {

        int n = createSeveralQuizzes(30);
        int limit = 10;

        ListDto<?> listDto = given()
                .queryParam("limit", limit)
                .get("/quizzes")
                .then()
                .statusCode(200)
                .extract()
                .as(ListDto.class);

        Set<String> first = getQuestions(listDto);

        //read next page
        ListDto<?> nextDto = given()
                .get(listDto._links.next.href)
                .then()
                .statusCode(200)
                .extract()
                .as(ListDto.class);

        Set<String> next = getQuestions(nextDto);
        // check that an element of next page was not in the first page
        assertTrue(!first.contains(next.iterator().next()));

        /*
            The "previous" page of the "next" page should be the
            first "self" page, ie

            self.next.previous == self
         */
        ListDto<?> previousDto = given()
                .get(nextDto._links.previous.href)
                .then()
                .statusCode(200)
                .extract()
                .as(ListDto.class);

        Set<String> previous = getQuestions(previousDto);
        assertContainsTheSame(first, previous);
    }

    @Test
    public void testWrongOffset() {

        given().queryParam("offset", -1)
                .queryParam("limit", 10)
                .get("/quizzes")
                .then()
                .statusCode(400);
    }

    @Test
    public void testWrongLimit() {

        given().queryParam("offset", 0)
                .queryParam("limit", -10)
                .get("/quizzes")
                .then()
                .statusCode(400);
    }

    @Test
    public void testFilter(){

        String sub = "bar";
        String subId = createCtgAndSubAndGetIdOfSub("foo", sub);

        given().contentType(Formats.JSON_V1)
                .body(new CreateQuizDto(subId, "q", "a0", "a1", "a2", "a3", 0))
                .post("/quizzes")
                .then()
                .statusCode(201);


        given().queryParam("limit", 10)
                .get("/quizzes")
                .then()
                .statusCode(200)
                .body("totalSize", is(1));

        given().queryParam("limit", 10)
                .queryParam("filter", subId)
                .get("/quizzes")
                .then()
                .statusCode(200)
                .body("totalSize", is(1));

        given().queryParam("limit", 10)
                .queryParam("filter", -42)
                .get("/quizzes")
                .then()
                .statusCode(200)
                .body("totalSize", is(0));
    }

    @Test
    public void testInvalidFilter(){

        String sub = "bar";
        createCtgAndSubAndGetIdOfSub("foo", sub);

        given().queryParam("limit", 10)
                .queryParam("filter", "not a number")
                .get("/quizzes")
                .then()
                .statusCode(400);
    }
}
