package org.pg6100.dropwizard.hystrix;

import com.netflix.hystrix.Hystrix;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by arcuri82 on 18-Nov-16.
 */
public class YRestTest {

    @ClassRule
    public static final DropwizardAppRule<HystrixExampleConfiguration> RULE =
            new DropwizardAppRule<>(HystrixExampleApplication.class);

    @BeforeClass
    public static void initRestAssured() {

        // RestAssured configs shared by all the tests
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/api/y";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }


    @Before
    public void reset() {
        /*
            NOTE: this works ONLY because we are running the RESTful service in the
            same JVM of the tests
         */
        Hystrix.reset();
    }


    private long exe(long value) {

        String res = given().queryParam("v", value)
                .accept(ContentType.ANY)
                .get("/single")
                .then()
                .statusCode(200)
                .extract().asString().trim();

        return Long.parseLong(res);
    }

    private long exe(long a,long b,long c,long d,long e) {

        String res = given()
                .queryParam("a", a)
                .queryParam("b", b)
                .queryParam("c", c)
                .queryParam("d", d)
                .queryParam("e", e)
                .accept(ContentType.ANY)
                .get("/multi")
                .then()
                .statusCode(200)
                .extract().asString().trim();

        return Long.parseLong(res);
    }

    @Test
    public void testBase() {

        long value = 50;
        long res = exe(value);

        assertEquals(value * 2, res);
    }

    @Test
    public void testTimeout() {

        long value = 600;
        long res = exe(value);

        assertEquals(0, res);
    }


    @Test
    public void testCircuitBreaker() {

        long value = 600;

        long start = System.currentTimeMillis();
        exe(value);
        exe(value); // this will trigger the circuit breaker
        long delta = System.currentTimeMillis() - start;

        assertTrue(delta > 1000); // the 2 calls took at least 1200ms

        start = System.currentTimeMillis();
        // these will all fail immediately, as CB is on.
        // if these were not failing immediately, would take 10*500 = 5s
        exe(value);
        exe(value);
        exe(value);
        exe(value);
        exe(value);
        exe(value);
        exe(value);
        exe(value);
        exe(value);
        exe(value);
        delta = System.currentTimeMillis() - start;

        assertTrue(delta < 1000); //likely even shorter than 1s
    }


    @Test
    public void testAsync(){

        long value = 300;

        //warm up
        exe(value, value, value, value, value);

        long start = System.currentTimeMillis();
        long res = exe(value, value, value, value, value);
        long delta = System.currentTimeMillis() - start;

        assertEquals(5 * value * 2 , res);
        assertTrue("Delta: "+delta, delta <  600);
    }
}