package org.pg6100.soap.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.nasdaqdod.ws.services.v1.MarketCenter;
import com.nasdaqdod.ws.services.v1.NASDAQQuotesSoap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class WsRecorder {

    /*
        Run it once to record actual XML message from the NASDAQ Web Service
     */

    public static void main(String[] args){

        try {
            int port = 8099;
            WireMockServer server = new WireMockServer(
                    wireMockConfig().port(port).notifier(new ConsoleNotifier(true))
            );
            Path m = Paths.get("target/wiremock/mappings");
            Path f = Paths.get("target/wiremock/files");
            Files.createDirectories(m);
            Files.createDirectories(f);
            FileSource mappings = new SingleRootFileSource(m.toFile().getAbsolutePath());
            FileSource files = new SingleRootFileSource(m.toFile().getAbsolutePath());
            server.enableRecordMappings(mappings,files);

            server.start();

            NASDAQQuotesSoap soap = WsProvider.getNASDAQQuotesSoap("localhost:" + port);

            server.stubFor(post(urlMatching(".*")).
                    willReturn(aResponse().proxiedFrom("http://" + WsProvider.NASDAQ_ADDRESS)));

            for (MarketCenter mc : soap.listMarketCenters().getMarketCenter()) {
                System.out.println(mc.getName());
            }

            server.stop();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}
