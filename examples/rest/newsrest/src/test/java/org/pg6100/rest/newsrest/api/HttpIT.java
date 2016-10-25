package org.pg6100.rest.newsrest.api;


import io.restassured.http.ContentType;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pg6100.rest.newsrest.dto.NewsDto;
import org.pg6100.utils.web.HttpUtil;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static io.restassured.RestAssured.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Note: these are not really "tests", but rather a simple way to
 * call the server via REST and check results in an automated way:
 * this is done to try out what are the return values of the different
 * HTTP verbs/methods
 */
public class HttpIT extends NewsRestTestBase {

    /*
        Note: here I am mixing RestAssured, RestEasy and direct TCP
     */

    private static UriBuilder base;
    private static Client client;

    @BeforeClass
    public static void initReasEasy(){
        base = UriBuilder.fromUri("http://localhost/newsrest/api/news")
                .port(8080);
        client = ClientBuilder.newClient();
    }


    private String createAPost(){
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

        return id;
    }

    @Test
    public void testOptions(){

        options().then()
                .statusCode(200)
                //Allow: HEAD, POST, GET, OPTIONS
                .header("Allow", containsString("GET"))
                .header("Allow", containsString("POST"))
                //note: having GET, will get HEAD by default, ie do not need to implement it
                .header("Allow", containsString("HEAD"))
                // this should always be available by default, ie no need to activate/implement it
                .header("Allow", containsString("OPTIONS"))
                //
                .header("Allow", not(containsString("DELETE")));
    }

    @Test
    public void testMissingVerb(){

        delete().then()
                .statusCode(405) //method not allowed
                .header("Allow", containsString("GET")) //response should tell what is allowed
                .header("Allow", containsString("POST"))
                .header("Allow", containsString("HEAD"))
                .header("Allow", containsString("OPTIONS"));
    }

    @Test
    public void testWrongVerb() throws Exception{

        /*
            THIS IS VERY IMPORTANT

            the rfc7231 states:

            "The 405 (Method Not Allowed) status code indicates that the method
             received in the request-line is known by the origin server but not
             supported by the target resource."

            "The 501 (Not Implemented) status code indicates that the server does
             not support the functionality required to fulfill the request. This
             is the appropriate response when the server does not recognize the
             request method"

             So if I make up a fake verb, eg FOO, I would expect 501.
             However, Wildfly returns 405.
             Why is this very important?
             Because you always have to keep in mind that the frameworks you are using
             can have BUGS!!!

             Is this a critical bug in Wildfly? Answer is NO.
             Actually, I do prefer what Wildfly is doing here, ie returning
             a 4xx error (user-error) instead of 5xx (server-error).

             I would go as far as to say that the "bug" is in the specs, ie
             501 should really be in the 4xx family
         */

        String http = "FOO /newsrest/api/news HTTP/1.1\n";
        http += "Host:localhost\n";
        http += "\n";

        String response = HttpUtil.executeHttpCommand("localhost", 8080, http);
        assertTrue("Response:\n" + response, response.contains("405"));
    }

    @Test
    public void testHead(){

        String id = createAPost();

        URI uri = base.path("/id/"+id).build();
        Response response = client.target(uri).request("application/json").get();
        assertEquals(200, response.getStatus());

        //here could use: getLength
        int length = Integer.parseInt(response.getHeaderString("Content-length"));

        assertTrue(response.hasEntity()); //check there is a body content

        byte[] data = response.readEntity(byte[].class);
        assertEquals(length, data.length);


        //now do HEAD
        response = client.target(uri).request("application/json").head();
        assertEquals(200, response.getStatus());

        //should still be the Content-length header with right length...
        int headLength = response.getLength();
        assertEquals(length, headLength);

        //... but no body content should had been sent
        assertFalse(response.hasEntity());
    }


    /*
        Note: there are also other 2 verbs:

        TRACE: for debugging when you a complex chain of proxies, eg to check what modifications
               they do on the messages. It is off by default.

        CONNECT: used in encryption, ie SSL


        There is also another verb called PATCH covered by rfc5789,
        which is used to do partial updates by sending a set of "instructions"
        on how to do such updates (which might not be idempotent)
     */
}
