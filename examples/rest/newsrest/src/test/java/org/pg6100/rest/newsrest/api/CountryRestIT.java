package org.pg6100.rest.newsrest.api;

import com.google.gson.Gson;
import io.restassured.http.ContentType;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pg6100.utils.web.HttpUtil;
import org.pg6100.utils.web.JBossUtil;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URI;
import java.util.List;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/*
    Need to run it as an integration test (IT) as we need Wildfly up and running
 */
public class CountryRestIT {

    @BeforeClass
    public static void initClass(){
        JBossUtil.waitForJBoss(10);
    }



    @Test
    public void testWithRawTcp() throws Exception {

        /*
            Here we build a "raw" HTTP request to get the list of countries,
            and do it by using low level TCP connections.
            Note: this is only for didactic purposes
         */

        // "verb" (GET in this case), followed by " ", then the path to resource, " ", and finally the protocol
        String request = "GET /newsrest/api/countries HTTP/1.1 \n";
        //headers are pairs <key>:<value>, where the key is case insensitive
        request += "Host:localhost \n";  //this is compulsory: a server running at an IP can serve different host names
        request += "Accept:application/json \n"; //we states that we want the resouce in Json format
        request += "\n"; //empty line indicates the end of the header section

        String result = HttpUtil.executeHttpCommand("localhost", 8080, request);
        System.out.println(result);

        String headers = HttpUtil.getHeaderBlock(result);
        assertTrue(headers.contains("200 OK"));

        String contentType = HttpUtil.getHeaderValue("Content-Type", result);
        assertTrue(contentType.contains("application/json"));

        String body = HttpUtil.getBodyBlock(result);
        assertTrue(body.contains("Norway"));
        assertTrue(body.contains("Sweden"));
        assertTrue(body.contains("Germany"));
    }

    @Test
    public void testWithApacheHttpClient() throws Exception {

        /*
            There are libraries to simplify the use of HTTP.
            The most popular is Apache HttpClient.
            Note: this is a generic HTTP library, and not specific for REST
         */

        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet("http://localhost:8080/newsrest/api/countries");
        httpGet.addHeader("Accept", "application/json");

        HttpResponse response = httpclient.execute(httpGet);
        assertEquals(200, response.getStatusLine().getStatusCode());

        String body = EntityUtils.toString(response.getEntity());
        System.out.println(body);

        Gson gson = new Gson();
        List<String> countries = gson.fromJson(body, List.class);

        assertTrue(countries.size() > 200);
        assertTrue(countries.contains("Norway"));
        assertTrue(countries.contains("Sweden"));
        assertTrue(countries.contains("Germany"));

        httpGet.releaseConnection();
    }

    @Test
    public void testWithRestEasy() {

        /*
            If you are dealing with REST, you might want to use a library that is
            specific for REST, like RestEasy.
            Note: this is the same library used internally in Wildfly to create
            REST services (ie, an implementation of JAX-RS).
         */

        URI uri = UriBuilder.fromUri("http://localhost/newsrest/api/countries")
                .port(8080)
                .build();
        Client client = ClientBuilder.newClient();

        Response response = client.target(uri).request("application/json").get();
        assertEquals(200, response.getStatus());

        String body = response.readEntity(String.class);

        Gson gson = new Gson();
        List<String> countries = gson.fromJson(body, List.class);

        assertTrue(countries.size() > 200);
        assertTrue(countries.contains("Norway"));
        assertTrue(countries.contains("Sweden"));
        assertTrue(countries.contains("Germany"));
    }

    @Test
    public void testWithRestAssured() {

        /*
            If your goal is testing REST API, there are libraries that
            are specialized in it. One is RestAssured.
            You can compare the code beneath with the other tests,
            and decide which is the easiest to read.

            One good thing of RestAssured is the ability to define
            assertions on the JSon responses in the body without
            having to manually parsing (or unmarshalling) it first.
         */

        given().accept(ContentType.JSON)
                .and()
                .get("http://localhost:8080/newsrest/api/countries")
                .then()
                .statusCode(200)
                .and()
                .body("size()", is(greaterThan(200)))
                .body(containsString("Norway"))
                .body(containsString("Sweden"))
                .body(containsString("Germany"));
    }
}