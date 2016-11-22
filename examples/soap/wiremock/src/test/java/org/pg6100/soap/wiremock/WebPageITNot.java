package org.pg6100.soap.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pg6100.utils.web.JBossUtil;
import org.pg6100.utils.web.WebTestBase;

import java.util.Scanner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class WebPageITNot extends WebTestBase {

    private static WireMockServer server;

    @BeforeClass
    public static void initWiremock() throws InterruptedException {

        server = new WireMockServer(
                wireMockConfig().port(8099).notifier(new ConsoleNotifier(true))
        );
        server.start();

        String body = new Scanner(WebPageITNot.class.getResourceAsStream("/body-v1-NASDAQQuotes.asmx.xml"), "UTF-8")
                .useDelimiter("\\A") //matches the beginning of input
                .next();

        server.stubFor(
                post(urlMatching("/v1/NASDAQQuotes.asmx"))
                        .withRequestBody(matching(".*ListMarketCenters.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type","text/xml;charset=utf-8")
                        .withHeader("Content-Length",""+body.length())
                        .withBody(body)
                ));
    }

    @AfterClass
    public static void tearDown(){
        server.stop();
    }


    @Before
    public void startFromInitialPage(){

        //if for any reason WildFly is not running any more, do not fail the tests
        assumeTrue(JBossUtil.isJBossUpAndRunning());

        getDriver().get("http://localhost:8080/soap_wm/home.faces");
        waitForPageToLoad();
    }


    @Test
    public void testShowRealCenter(){
        assertTrue(getPageSource().contains("The NASDAQ Stock Market LLC"));
    }

    @Test
    public void testShowFakeCenter() throws InterruptedException {
        //this will fail when run vs real Web Service
        assertTrue(getPageSource().contains("A Mocked Name"));

        //just to make it visible from Maven
        Thread.sleep(10_000);
    }

}
