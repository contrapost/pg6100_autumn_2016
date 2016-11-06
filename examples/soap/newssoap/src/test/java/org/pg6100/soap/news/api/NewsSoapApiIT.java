package org.pg6100.soap.news.api;


import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pg6100.soap.client.*;
import org.pg6100.utils.web.HttpUtil;

import javax.xml.ws.BindingProvider;
import java.util.List;


import static org.junit.Assert.*;

/**
 * Created by arcuri82 on 11/2/2016.
 */
public class NewsSoapApiIT {

    /*
        Here, to test an actual client of the SOAP
        service, we will use the client library that
        is automatically generated from the WSDL
     */

    private static NewsSoap ws;

    @BeforeClass
    public static void initClass() {

        /*
            If you look at the code of the generated WSDL at

            target/generated-sources/wsdl/NewsSoapImplService.wsdl

            you will notice the:

            <port name="NewsSoapImplPort" binding="tns:NewsSoapImplPortBinding">
                <soap:address location="REPLACE_WITH_ACTUAL_URL"/>
            </port>

            this is not an error. When you create a WAR, you do not know where
            it will be deployed. So cannot specify the "soap:address".
            However, when deployed in a JEE container like Wildfly,
            then Wildfly will replace the "location" with the actual
            value of where it is running.

            As we use the WSDL before it is deployed to Wildfly,
            we need to "hack" the generated client library by modifying
            the "location" on the fly.
         */

        NewsSoapImplService service = new NewsSoapImplService();
        ws = service.getNewsSoapImplPort();

        String url = "http://localhost:8080/newssoap/NewsSoapImpl";

        ((BindingProvider)ws).getRequestContext().put(
                BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
    }

    @Before @After
    public void cleanData(){

        /*
            Note: this DTO is not the one we wrote, but the
            one automatically generated from the WSDL under

            target/generated-sources/test-wsimport/org/pg6100/soap/client
         */

        List<NewsDto> list = ws.get(null, null);

        list.stream().forEach(dto -> ws.deleteNews(dto.getNewsId()));
    }

    @Test
    public void testCleanDB() {

        List<NewsDto> list = ws.get(null, null);

        assertEquals(0, list.size());
    }

    @Test
    public void testCreateAndGet() {

        assertEquals(0, ws.get(null, null).size());

        String author = "author";
        String text = "someText";
        String country = "Norway";
        NewsDto dto = new NewsDto();
        dto.setAuthorId(author);
        dto.setText(text);
        dto.setCountry(country);


        Long id = ws.createNews(dto);

        assertEquals(1, ws.get(null, null).size());

        NewsDto res = ws.getNews(id);

        assertEquals(id,      res.getNewsId());
        assertEquals(author,  res.getAuthorId());
        assertEquals(text,    res.getText());
        assertEquals(country, res.getCountry());
    }


    @Test
    public void testException(){

        String author = "";
        String text = "";
        String country = "foo";  //invalid country
        NewsDto dto = new NewsDto();
        dto.setAuthorId(author);
        dto.setText(text);
        dto.setCountry(country);

        try {
            ws.createNews(dto);
            fail(); //recall, this throws an Error and not an Exception
        } catch (Exception e){
            //expected
        }
    }


    /*
        How do SOAP messages look like?
        First of all, in contrast to REST, SOAP does not
        depend on HTTP. Most of the times, it is used
        with HTTP, but it is not a requirement.

        All requests, ie remote method invocations, are
        done with POST messages to the same URL,
        where the instructions of
        what should executed is inside an XML message.

        Each message is inside a <Envelope><Body> SOAP wrapper.

        Note: if you want to look and monitor the actual HTTP messages,
        you can use the WireShark tool, downloadable at:

        https://www.wireshark.org/

        and sniff packets on loopback address (ie 127.0.0.1) with
        filter:

        http && tcp.port==8080

     */

    @Test
    public void testRawGet() throws Exception{

        /*
            Note the use of XML namespaces (xmlns).
            This is essential to distinguish between the SOAP wrapper
            tags and the XML message tags in case of name conflicts.
         */
        String body = "";
        body += "<?xml version='1.0'?>";
        body += "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">";
        body += "  <S:Body>";
        body += "    <ns2:get xmlns:ns2='http://api.news.soap.pg6100.org/'>";
        body += "    </ns2:get>";
        body += "  </S:Body>";
        body += "</S:Envelope>";

        String headers = "";
        headers += "POST /newssoap/NewsSoapImpl HTTP/1.1\r\n";
        headers += "Host: localhost:8080\r\n";
        headers += "Accept: text/xml\r\n";
        //Note: this header is used to specify which method will be called
        headers += "SOAPAction: \"http://api.news.soap.pg6100.org/NewsSoap/getRequest\"\r\n";
        headers += "Content-Length: " + body.getBytes("UTF-8").length + "\r\n";
        headers += "\r\n";

        String message = headers + body;

        String response = HttpUtil.executeHttpCommand("localhost", 8080, message);

        assertTrue(HttpUtil.getHeaderBlock(response).contains("200 OK"));

        assertTrue(HttpUtil.getBodyBlock(response).contains("Envelope"));
        assertTrue(HttpUtil.getBodyBlock(response).contains("Body"));
        assertTrue(HttpUtil.getBodyBlock(response).contains("getResponse"));

        /*
            Actual full response:

            HTTP/1.1 200 OK\r\n
            Connection: keep-alive\r\n
            X-Powered-By: Undertow/1\r\n
            Server: WildFly/10\r\n
            Content-Type: text/xml;charset=UTF-8\r\n
            Content-Length: 172\r\n
            Date: Sun, 06 Nov 2016 08:47:04 GMT\r\n
            \r\n
            <soap:Envelope
                xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                <soap:Body>
                    <ns2:getResponse
                        xmlns:ns2="http://api.news.soap.pg6100.org/"/>
                </soap:Body>
            </soap:Envelope>
         */
    }


    @Test
    public void testRawException() throws Exception{

        String body = "";
        body += "<?xml version='1.0' ?>";
        body += "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'>";
        body += "  <S:Body>";
        body += "    <ns2:createNews xmlns:ns2='http://api.news.soap.pg6100.org/'>";
        body += "      <arg0>";
        body += "         <authorId></authorId>";
        body += "         <text></text>";
        body += "          <country>foo</country>";
        body += "      </arg0>";
        body += "   </ns2:createNews>";
        body += "  </S:Body>";
        body += "</S:Envelope>";

        String headers = "";
        headers += "POST /newssoap/NewsSoapImpl HTTP/1.1\r\n";
        headers += "Host: localhost:8080\r\n";
        headers += "Accept: text/xml\r\n";
        headers += "Content-Type: text/xml; charset=utf-8\r\n";
        headers += "SOAPAction: \"http://api.news.soap.pg6100.org/NewsSoap/createNewsRequest\"\r\n";
        headers += "Content-Length: " + body.getBytes("UTF-8").length + "\r\n";
        headers += "\r\n";

        String message = headers + body;

        String response = HttpUtil.executeHttpCommand("localhost", 8080, message);

        /*
            SOAP does not care of the HTTP error code.
            If there is a problem on server, SOAP would still expect a
            response where in the body there is valid SOAP XML stating
            what failure did happen.

            Note: Wildfly will automatically generate such XML when
            exceptions are uncaught in the web service.
         */
        assertTrue(HttpUtil.getHeaderBlock(response).contains("500"));

        assertTrue(HttpUtil.getBodyBlock(response).contains("Envelope"));
        assertTrue(HttpUtil.getBodyBlock(response).contains("Body"));
        assertTrue(HttpUtil.getBodyBlock(response).contains("Fault"));
        assertTrue(HttpUtil.getBodyBlock(response).contains("Transaction rolled back"));


        /*
            Actual full response:

            HTTP/1.1 500 Internal Server Error\r\n
            Connection: keep-alive\r\n
            X-Powered-By: Undertow/1\r\n
            Server: WildFly/10\r\n
            Content-Type: text/xml;charset=UTF-8\r\n
            Content-Length: 218\r\n
            Date: Sun, 06 Nov 2016 09:06:31 GMT\r\n
            \r\n
            <soap:Envelope
                xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                <soap:Body>
                    <soap:Fault>
                        <faultcode>
                            soap:Server
                        </faultcode>
                    <faultstring>
                        Transaction rolled back
                    </faultstring>
                </soap:Fault>
                </soap:Body>
            </soap:Envelope>
         */
    }
}