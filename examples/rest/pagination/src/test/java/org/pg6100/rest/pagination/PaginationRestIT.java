package org.pg6100.rest.pagination;

import io.restassured.RestAssured;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pg6100.rest.pagination.dto.base.CommentDto;
import org.pg6100.rest.pagination.dto.base.NewsDto;
import org.pg6100.rest.pagination.dto.base.VoteDto;
import org.pg6100.rest.pagination.dto.collection.ListDto;
import org.pg6100.utils.web.JBossUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class PaginationRestIT {


    @BeforeClass
    public static void initClass() {
        JBossUtil.waitForJBoss(10);

        // RestAssured configs shared by all the tests
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/pagination/api/news";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }


    @Before
    @After
    public void clean() {

        int total = Integer.MAX_VALUE;

        /*
            as the REST API does not return the whole state of the database (even,
            if I use an infinite "limit") I need to keep doing queries until the totalSize is 0
         */

        while (total > 0) {

            //seems there are some limitations when handling generics
            ListDto<?> listDto = given()
                    .queryParam("limit", Integer.MAX_VALUE)
                    .get()
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(ListDto.class);

            listDto.list.stream()
                    //the "NewsDto" get unmarshalled into a map of fields
                    .map(n -> ((Map) n).get("id"))
                    .forEach(id ->
                            given().delete("/" + id)
                                    .then()
                                    .statusCode(204)
                    );

            total = listDto.totalSize - listDto.list.size();
        }
    }

    @Test
    public void testBaseCreateNews() {

        String text = "some text";
        String country = "Norway";

        String location = given().body(new NewsDto(text, country, null, null))
                .contentType(Format.JSON_V1)
                .post()
                .then()
                .statusCode(201)
                .extract().header("location");

        given().get()
                .then()
                .statusCode(200)
                .body("list.size()", is(1))
                .body("totalSize", is(1));

        given().get(location)
                .then()
                .statusCode(200)
                .body("text", is(text))
                .body("country", is(country));
    }


    private void createNews(String text, String country) {

        given().body(new NewsDto(text, country, null, null))
                .contentType(Format.JSON_V1)
                .post()
                .then()
                .statusCode(201);
    }

    private int createSeveralNews(int n){

        for (int i = 0; i < n; i++) {
            createNews("" + i, "Norway");
        }

        return n;
    }

    /**
     * Extract the "text" fields from all the News
     */
    private Set<String> getTexts(ListDto<?> selfDto) {

        Set<String> values = new HashSet<>();
        selfDto.list.stream()
                .map(m -> (String) ((Map) m).get("text"))
                .forEach(t -> values.add(t));

        return values;
    }

    private void assertContainsTheSame(Collection<?> a, Collection<?> b) {
        assertEquals(a.size(), b.size());
        a.stream().forEach(v -> assertTrue(b.contains(v)));
        b.stream().forEach(v -> assertTrue(a.contains(v)));
    }


    @Test
    public void testSelfLink() {

        int n = createSeveralNews(30);
        int limit = 10;

        ListDto<?> listDto = given()
                .queryParam("limit", limit)
                .get()
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

        Set<String> first = getTexts(listDto);
        Set<String> self = getTexts(selfDto);

        assertContainsTheSame(first, self);
    }


    @Test
    public void testNextLink() {

        int n = createSeveralNews(30);
        int limit = 10;

        ListDto<?> listDto = given()
                .queryParam("limit", limit)
                .get()
                .then()
                .statusCode(200)
                .extract()
                .as(ListDto.class);

        assertEquals(n, (int) listDto.totalSize);
        assertNotNull(listDto._links.next.href);

        Set<String> values = getTexts(listDto);
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

            values.addAll(getTexts(listDto));

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

        int n = createSeveralNews(30);
        int limit = 10;

        ListDto<?> listDto = given()
                .queryParam("limit", limit)
                .get()
                .then()
                .statusCode(200)
                .extract()
                .as(ListDto.class);

        Set<String> first = getTexts(listDto);

        //read next page
        ListDto<?> nextDto = given()
                .get(listDto._links.next.href)
                .then()
                .statusCode(200)
                .extract()
                .as(ListDto.class);

        Set<String> next = getTexts(nextDto);
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

        Set<String> previous = getTexts(previousDto);
        assertContainsTheSame(first, previous);
    }


    private void createNewsWithCommentAndVote() {
        String newsLocation = given().body(new NewsDto("some text", "Sweden", null, null))
                .contentType(Format.JSON_V1)
                .post()
                .then()
                .statusCode(201)
                .extract().header("location");

        given().body(new VoteDto("a user"))
                .contentType(Format.JSON_V1)
                .post(newsLocation + "/votes")
                .then()
                .statusCode(201);

        given().body(new CommentDto("a comment"))
                .contentType(Format.JSON_V1)
                .post(newsLocation + "/comments")
                .then()
                .statusCode(201);
    }


    private void getWithExpand(String expandType, int expectedComments, int expectedVotes){

        given().queryParam("expand", expandType)
                .get()
                .then()
                .statusCode(200)
                .body("list.size()", is(1))
                .body("list.comments.id.size()", is(expectedComments))
                .body("list.votes.id.size()", is(expectedVotes))
                .body("_links.self.href", containsString("expand="+expandType));
    }

    @Test
    public void testExpandNone() {

        createNewsWithCommentAndVote();
        getWithExpand("NONE", 0 , 0);
    }

    @Test
    public void testExpandALL() {

        createNewsWithCommentAndVote();
        getWithExpand("ALL", 1 , 1);
    }

    @Test
    public void testExpandComments() {

        createNewsWithCommentAndVote();
        getWithExpand("COMMENTS", 1 , 0);
    }

    @Test
    public void testExpandVotes() {

        createNewsWithCommentAndVote();
        getWithExpand("VOTES", 0 , 1);
    }



    @Test
    public void testNegativeOffset(){

        given().queryParam("offset", -1)
                .get()
                .then()
                .statusCode(400);
    }

    @Test
    public void testInvalidOffset(){

        given().queryParam("offset", 0)
                .get()
                .then()
                .statusCode(200);

        given().queryParam("offset", 1)
                .get()
                .then()
                .statusCode(400);
    }

    @Test
    public void testInvalidLimit(){

        given().queryParam("limit", 0)
                .get()
                .then()
                .statusCode(400);
    }


    @Test
    public void testInvalidExpand(){

        /*
            Slightly tricky: in the code we use "expand" as a Java
            enumeration. But, in the URI request, it is just a string.
            So what happens if the string does not match any valid value
            in the enum? Technically, it should be a 4xx user error.
            Wildfly decides to use 404, but other JAX-RS implementations
            (ie RestEasy vs Jersey) "might" use a different code.
         */

        given().queryParam("expand", "foo")
                .get()
                .then()
                .statusCode(404);


        /*
            however, what if you just use a param that does not exist?
            it just gets ignored...
            so the behavior is actually quite inconsistent
         */

        given().queryParam("bar", "foo")
                .get()
                .then()
                .statusCode(200);
    }
}
