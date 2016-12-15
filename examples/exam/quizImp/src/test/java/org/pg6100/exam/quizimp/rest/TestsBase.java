package org.pg6100.exam.quizimp.rest;

import io.restassured.RestAssured;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.pg6100.exam.quizapi.Formats;
import org.pg6100.exam.quizapi.dto.*;
import org.pg6100.exam.quizapi.dto.hal.ListDto;
import org.pg6100.utils.web.JBossUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Created by arcuri82 on 02-Dec-16.
 */
public abstract class TestsBase {

    @BeforeClass
    public static void baseInit() {
        JBossUtil.waitForJBoss(10);

        // RestAssured configs shared by all the tests
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/quiz/api/";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Before
    @After
    public void cleanState() {

        List<ReadCategoryDto> categories = Arrays.asList(given()
                .accept(Formats.JSON_V1)
                .get("/categories")
                .then()
                .statusCode(200)
                .extract().as(ReadCategoryDto[].class));

        categories.stream().forEach(c ->
                given().delete("/categories/" + c.id)
                        .then()
                        .statusCode(204)
        );


        int total = Integer.MAX_VALUE;

        while (total > 0) {

            //seems there are some limitations when handling generics
            ListDto<?> listDto = given()
                    .queryParam("limit", Integer.MAX_VALUE)
                    .get("/quizzes")
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(ListDto.class);

            listDto.list.stream()
                    .map(n -> ((Map) n).get("id"))
                    .forEach(id ->
                            given().delete("/quizzes/" + id)
                                    .then()
                                    .statusCode(204)
                    );

            total = listDto.totalSize - listDto.list.size();
        }
    }

    protected String createCategory(String name) {

        return given().contentType(Formats.JSON_V1)
                .body(new CreateCategoryDto(name))
                .post("/categories")
                .then()
                .statusCode(201)
                .extract()
                .header("location");
    }

    protected String createCategoryAndGetId(String name) {

        String location = createCategory(name);

        return given().accept(Formats.JSON_V1)
                .get(location)
                .then()
                .statusCode(200)
                .extract()
                .body().as(ReadCategoryDto.class)
                .id;
    }

    protected String createCtgAndSubAndGetIdOfSub(String categoryName, String subcategoryName){

        String ctgLocation = createCategory(categoryName);
        String subLocation = createSubCategory(ctgLocation, subcategoryName);

        return given().accept(Formats.JSON_V1)
                .get(subLocation)
                .then()
                .statusCode(200)
                .extract()
                .body().as(ReadSubCategoryDto.class)
                .id;
    }

    protected String createSubCategory(String parentUri, String name) {

        return given().contentType(Formats.JSON_V1)
                .body(new CreateSubCategoryDto(name))
                .post(parentUri + "/subcategories")
                .then()
                .statusCode(201)
                .extract()
                .header("location");
    }
}
