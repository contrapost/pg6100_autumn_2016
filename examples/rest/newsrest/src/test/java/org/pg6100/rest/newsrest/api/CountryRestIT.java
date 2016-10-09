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

public class CountryRestIT {

    @BeforeClass
    public static void initClass(){
        JBossUtil.waitForJBoss(10);
    }


    private String executeHttpCommand(String host, int port, String request) throws Exception {

        try (Socket socket = new Socket(host, port)) {
            socket.getOutputStream().write(request.getBytes());
            socket.shutdownOutput();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            String response = "";
            String line = in.readLine();

            while (line != null) {
                response += line + "\n";
                line = in.readLine();
            }
            return response;
        }
    }

    @Test
    public void testWithRawTcp() throws Exception {

        String request = "GET /newsrest/api/country HTTP/1.1 \n";
        request += "Host:localhost \n";
        request += "Accept:application/json \n";
        request += "\n";

        String result = executeHttpCommand("localhost", 8080, request);
        System.out.println(result);

        assertTrue(result.contains("200 OK"));
        assertTrue(result.contains("Norway"));
        assertTrue(result.contains("Sweden"));
        assertTrue(result.contains("Germany"));
    }

    @Test
    public void testWithApacheHttpClient() throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet("http://localhost:8080/newsrest/api/country");
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
    }

    @Test
    public void testWithRestEasy() {
        URI uri = UriBuilder.fromUri("http://localhost/newsrest/api/country").port(8080).build();
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

        given().accept(ContentType.JSON)
                .and()
                .get("http://localhost:8080/newsrest/api/country")
                .then()
                .statusCode(200)
                .body("size()", is(greaterThan(200)))
                .body(containsString("Norway"))
                .body(containsString("Sweden"))
                .body(containsString("Germany"));
    }
}