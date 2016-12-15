package org.pg6100.exam.quizimp.rest;

import io.restassured.RestAssured;
import org.hamcrest.collection.IsArrayContaining;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pg6100.exam.quizapi.Formats;
import org.pg6100.exam.quizapi.dto.CreateCategoryDto;
import org.pg6100.exam.quizapi.dto.CreateSubCategoryDto;
import org.pg6100.exam.quizapi.dto.ReadCategoryDto;
import org.pg6100.utils.web.HttpUtil;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;


/**
 * Created by arcuri82 on 02-Dec-16.
 */
public class CategoryRestIT extends TestsBase {


    @Test
    public void testCreateCategory() {

        String name = "foo";
        String location = createCategory(name);

        given().accept(Formats.JSON_V1)
                .get(location)
                .then()
                .statusCode(200)
                .body("name", is(name));
    }

    @Test
    public void testCreateCategoryRawHttp() throws Exception{

        String name = "økologi";
        String  postbodyJson = "{ \"name\": \""+name+"\"}";

        String postMessage = "POST /quiz/api/categories HTTP/1.1\r\n";
        postMessage += "Host:localhost:8080 \r\n";
        postMessage += "Content-type: "+Formats.JSON_V1 + "\r\n";
        postMessage += "Content-Length: " + postbodyJson.getBytes("UTF-8").length + "\r\n";
        postMessage += "\r\n";
        postMessage += postbodyJson;

        String response = HttpUtil.executeHttpCommand("localhost", 8080, postMessage);
        String location = HttpUtil.getHeaderValue("location", response);

        given().accept(Formats.JSON_V1)
                .get(location)
                .then()
                .statusCode(200)
                .body("name", is(name));
    }

    @Test
    public void testCreateTwoCategories() {

        String a = "a";
        String b = "b";

        createCategory(a);
        createCategory(b);

        given().accept(Formats.JSON_V1)
                .get("/categories")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("name", containsInAnyOrder(a, b));
    }


    @Test
    public void testCreateSubCategory() {

        String c = "foo";
        String sub = "bar";
        String categoryLocation = createCategory(c);
        String location = createSubCategory(categoryLocation, sub);

        given().accept(Formats.JSON_V1)
                .get(location)
                .then()
                .statusCode(200)
                .body("name", is(sub));
    }

    @Test
    public void testExpand() {

        String c = "foo";
        String sub = "bar";
        String categoryLocation = createCategory(c);
        createSubCategory(categoryLocation, sub);

        given().accept(Formats.JSON_V1)
                .queryParam("expand", false)
                .get("/categories")
                .then()
                .statusCode(200)
                .body("subCategories[0]", nullValue());

        given().accept(Formats.JSON_V1)
                .queryParam("expand", true)
                .get("/categories")
                .then()
                .statusCode(200)
                .body("subCategories[0][0].name", is(sub));

        given().accept(Formats.JSON_V1)
                .queryParam("expand", false)
                .get(categoryLocation)
                .then()
                .statusCode(200)
                .body("subCategories", nullValue());

        given().accept(Formats.JSON_V1)
                .queryParam("expand", true)
                .get(categoryLocation)
                .then()
                .statusCode(200)
                .body("subCategories[0].name", is(sub));
    }

    @Test
    public void testUpdateCategory() {

        String name = "foo";
        String location = createCategory(name);

        String changedName = name + " changed";

        given().contentType(Formats.MERGE_PATCH_V1)
                .body("{\"name\":\"" + changedName + "\"}")
                .patch(location)
                .then()
                .statusCode(204);

        given().accept(Formats.JSON_V1)
                .get(location)
                .then()
                .statusCode(200)
                .body("name", is(changedName));
    }


    @Test
    public void testGetSubCategories() {

        String c0 = createCategoryAndGetId("c0");
        String c1 = createCategoryAndGetId("c1");

        String s0a = "s0a";
        String s1a = "s1a";
        String s1b = "s1b";

        createSubCategory("/categories/"+c0, s0a);
        createSubCategory("/categories/"+c1, s1a);
        createSubCategory("/categories/"+c1, s1b);

        given().accept(Formats.JSON_V1)
                .get("/subcategories")
                .then()
                .statusCode(200)
                .body("size()", is(3));

        given().accept(Formats.JSON_V1)
                .queryParam("parentId", c0)
                .get("/subcategories")
                .then()
                .statusCode(200)
                .body("size()", is(1));

        given().accept(Formats.JSON_V1)
                .get("/categories/"+c1+"/subcategories")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }
}