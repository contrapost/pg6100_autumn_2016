package org.pg6100.rest.patch;

import io.restassured.http.ContentType;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pg6100.utils.web.HttpUtil;
import org.pg6100.utils.web.JBossUtil;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.*;
import static org.hamcrest.core.Is.is;

public class CounterRestIT {

    @BeforeClass
    public static void initClass(){
        JBossUtil.waitForJBoss(10);
    }


    @Test
    public void testCreate() throws Exception{

        String charset = "UTF-8";

        String name = "Ø counter";
        int value = 5;

        String body = "{ \"value\":"+value+", \"name\":\""+name+"\"}";
        byte[] data = body.getBytes(charset);
        int size = data.length;

        /*
            note: Ø will take two bytes in UTF-8 format.
            The "Content-Length" is expressed in octets, ie bytes,
            not the length of the Java string.
         */
        assertEquals(body.length() + 1, size);

        String baseURI="/patch/api/counters";

        String message = "POST "+baseURI+" HTTP/1.1\n";
        message += "Host:localhost:8080\n";
        message += "Content-Type:application/json; charset="+charset+"\n";
        message += "Content-Length:"+size+"\n";
        message += "\n";
        message += body;

        String response = HttpUtil.executeHttpCommand("localhost",8080, message);
        String headers = HttpUtil.getHeaderBlock(response);

        assertTrue(headers, headers.contains("201 Created"));

        //Response should contain header telling where the newly created
        //resource is available
        String location = HttpUtil.getHeaderValue("Location", headers);
        assertTrue(location, location.contains(baseURI));

        //extract the last path element, ie the {id} in /patch/api/counters/{id}
        int locationId = Integer.parseInt(
                location.substring(
                        location.lastIndexOf('/')+1,
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

    private void createNew(CounterDto dto){

        given().port(8080)
                .baseUri("http://localhost")
                .contentType(ContentType.JSON)
                .body(dto)
                .post("/patch/api/counters")
                .then()
                .statusCode(201);
    }

    private CounterDto get(long id){

        return given().port(8080)
                .baseUri("http://localhost")
                .accept(ContentType.JSON)
                .get("/patch/api/counters/"+id)
                .then()
                .statusCode(200)
                .extract()
                .as(CounterDto.class);
    }

    private void update(CounterDto dto){

        given().port(8080)
                .baseUri("http://localhost")
                .contentType(ContentType.JSON)
                .body(dto)
                .put("/patch/api/counters/"+dto.id)
                .then()
                .statusCode(204);
    }

    private void patchWithMergeJSon(long id, String jsonBody, int statusCode){
        given().port(8080)
                .baseUri("http://localhost")
                .contentType("application/merge-patch+json")
                .body(jsonBody)
                .patch("/patch/api/counters/"+id)
                .then()
                .statusCode(statusCode);
    }

    private void verifyEqual(CounterDto a, CounterDto b){
        assertEquals(a.id, b.id);
        assertEquals(a.name, b.name);
        assertEquals(a.value, b.value);
    }


    @Test
    public void  testPut(){

        CounterDto dto = new CounterDto(System.currentTimeMillis(),"foo", 5);
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
    public void testPatchCustomDelta(){

        int value = 46;

        CounterDto dto = new CounterDto(System.currentTimeMillis(),"foo", value);
        createNew(dto);

        int delta = -4;

        given().port(8080)
                .baseUri("http://localhost")
                .contentType(ContentType.TEXT)
                .body(delta)
                .patch("/patch/api/counters/"+dto.id)
                .then()
                .statusCode(204);

        CounterDto readBack = get(dto.id);
        assertEquals(value + delta, (int) readBack.value);
    }


    @Test
    public void testPatchMergeJSon(){

        long id = System.currentTimeMillis();
        int value = 55;
        String name = "foo";

        CounterDto dto = new CounterDto(id,name, value);
        createNew(dto);

        int modifiedValue = value * 3;

        //delete the name, and change the value
        patchWithMergeJSon(id, "{\"name\":null, \"value\":"+modifiedValue+"}", 204);

        CounterDto readBack = get(dto.id);
        assertEquals(modifiedValue, (int) readBack.value);
        assertNull(readBack.name);
        assertEquals(id, (long) readBack.id); // should had stayed the same
    }

    @Test
    public void testPatchMergeJSonJustValue(){

        long id = System.currentTimeMillis();
        int value = 55;
        String name = "foo";

        CounterDto dto = new CounterDto(id,name, value);
        createNew(dto);

        int modifiedValue = value * 3;

        //just change the value
        patchWithMergeJSon(id, "{\"value\":"+modifiedValue+"}", 204);

        CounterDto readBack = get(dto.id);
        assertEquals(modifiedValue, (int) readBack.value);
        assertEquals(name, readBack.name); //not modified
        assertEquals(id, (long) readBack.id); // should had stayed the same
    }

    @Test
    public void testPatchMergeJSonInvalidValue(){

        long id = System.currentTimeMillis();
        int value = 55;
        String name = "foo";

        CounterDto dto = new CounterDto(id,name, value);
        createNew(dto);

        int modifiedValue = value * 3;
        String modifiedName = "modified from " + name;

        //name is correct, but value is wrongly passed as string, not integer
        patchWithMergeJSon(id, "{\"name\":\""+modifiedName+"\", \"value\":\""+modifiedValue+"\"}", 400);

        CounterDto readBack = get(dto.id);
        //nothing should had been modified, as value was invalid
        assertEquals(value, (int) readBack.value);
        assertEquals(name, readBack.name); //IMPORTANT that this did not change
        assertEquals(id, (long) readBack.id);
    }

}