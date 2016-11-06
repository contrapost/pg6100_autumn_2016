package org.pg6100.dropwizard.dwcounter;

import io.dropwizard.testing.junit.DropwizardAppRule;
import io.restassured.http.ContentType;
import org.junit.ClassRule;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class CounterRestIT {

    /*
        This is a custom JUnit rule defined by DropWizard to start
        the Jetty server before the test suite, and stop it afterwards.

        Note: in contrast to Arquillian, the tests here run in the same
        process as the test runner, together with Jetty.

        So, you do not need any special configuration to run these tests!
        Furthermore, you can use a debugger directly, instead of a remote
        one, as there is only one process running.
     */

    @ClassRule
    public static final DropwizardAppRule<CounterConfiguration> RULE =
            new DropwizardAppRule<>(CounterApplication.class);

    @Test
    public void testSwagger(){

        given().baseUri("http://localhost")
                .port(8080)
                .accept(ContentType.JSON)
                .get("/patch/api/swagger.json")
                .then()
                .statusCode(200);
    }

    /*
        Following is practically a copy&paste from "rest/patch".
        Just changed the parts with raw HTTP messages
     */

    @Test
    public void testCreate() throws Exception {

        String basePath = "/patch/api/counters";

        String name = "Ø counter";
        int value = 5;

        String location = given()
                .baseUri("http://localhost")
                .port(8080)
                .contentType(ContentType.JSON)
                .body(new CounterDto(null, name, value))
                .post(basePath)
                .then()
                .statusCode(201)
                .extract()
                .header("Location");

        assertTrue(location, location.contains(basePath));

        //extract the last path element, ie the {id} in /patch/api/counters/{id}
        int locationId = Integer.parseInt(
                location.substring(
                        location.lastIndexOf('/') + 1,
                        location.length()));

        //to check the GET, now just using Rest-Assured
        given().port(8080)
                .accept(ContentType.JSON)
                .get(location)
                .then()
                .statusCode(200)
                .body("value", is(value))
                .body("name", is(name))
                .body("id", is(locationId));
    }

    private void createNew(CounterDto dto) {

        given().port(8080)
                .baseUri("http://localhost")
                .contentType(ContentType.JSON)
                .body(dto)
                .post("/patch/api/counters")
                .then()
                .statusCode(201);
    }

    private CounterDto get(long id) {

        return given().port(8080)
                .baseUri("http://localhost")
                .accept(ContentType.JSON)
                .get("/patch/api/counters/" + id)
                .then()
                .statusCode(200)
                .extract()
                .as(CounterDto.class);
    }

    private void update(CounterDto dto) {

        given().port(8080)
                .baseUri("http://localhost")
                .contentType(ContentType.JSON)
                .body(dto)
                .put("/patch/api/counters/" + dto.id)
                .then()
                .statusCode(204);
    }

    private void patchWithMergeJSon(long id, String jsonBody, int statusCode) {
        given().port(8080)
                .baseUri("http://localhost")
                .contentType("application/merge-patch+json")
                .body(jsonBody)
                .patch("/patch/api/counters/" + id)
                .then()
                .statusCode(statusCode);
    }

    private void verifyEqual(CounterDto a, CounterDto b) {
        assertEquals(a.id, b.id);
        assertEquals(a.name, b.name);
        assertEquals(a.value, b.value);
    }


    @Test
    public void testPut() {

        CounterDto dto = new CounterDto(System.currentTimeMillis(), "foo", 5);
        createNew(dto);

        CounterDto readBack = get(dto.id);
        verifyEqual(dto, readBack);

        CounterDto updated = new CounterDto(dto.id, null, 0);
        update(updated);
        readBack = get(dto.id);

        //whole replacement
        verifyEqual(updated, readBack);
    }

    @Test
    public void testPatchCustomDelta() {

        int value = 46;

        CounterDto dto = new CounterDto(System.currentTimeMillis(), "foo", value);
        createNew(dto);

        int delta = -4;

        given().port(8080)
                .baseUri("http://localhost")
                .contentType(ContentType.TEXT)
                .body(delta)
                .patch("/patch/api/counters/" + dto.id)
                .then()
                .statusCode(204);

        CounterDto readBack = get(dto.id);
        assertEquals(value + delta, (int) readBack.value);
    }


    @Test
    public void testPatchMergeJSon() {

        long id = System.currentTimeMillis();
        int value = 55;
        String name = "foo";

        CounterDto dto = new CounterDto(id, name, value);
        createNew(dto);

        int modifiedValue = value * 3;

        //delete the name, and change the value
        patchWithMergeJSon(id, "{\"name\":null, \"value\":" + modifiedValue + "}", 204);

        CounterDto readBack = get(dto.id);
        assertEquals(modifiedValue, (int) readBack.value);
        assertNull(readBack.name);
        assertEquals(id, (long) readBack.id); // should had stayed the same
    }

    @Test
    public void testPatchMergeJSonJustValue() {

        long id = System.currentTimeMillis();
        int value = 55;
        String name = "foo";

        CounterDto dto = new CounterDto(id, name, value);
        createNew(dto);

        int modifiedValue = value * 3;

        //just change the value
        patchWithMergeJSon(id, "{\"value\":" + modifiedValue + "}", 204);

        CounterDto readBack = get(dto.id);
        assertEquals(modifiedValue, (int) readBack.value);
        assertEquals(name, readBack.name); //not modified
        assertEquals(id, (long) readBack.id); // should had stayed the same
    }

    @Test
    public void testPatchMergeJSonInvalidValue() {

        long id = System.currentTimeMillis();
        int value = 55;
        String name = "foo";

        CounterDto dto = new CounterDto(id, name, value);
        createNew(dto);

        int modifiedValue = value * 3;
        String modifiedName = "modified from " + name;

        //name is correct, but value is wrongly passed as string, not integer
        patchWithMergeJSon(id, "{\"name\":\"" + modifiedName + "\", \"value\":\"" + modifiedValue + "\"}", 400);

        CounterDto readBack = get(dto.id);
        //nothing should had been modified, as value was invalid
        assertEquals(value, (int) readBack.value);
        assertEquals(name, readBack.name); //IMPORTANT that this did not change
        assertEquals(id, (long) readBack.id);
    }
}