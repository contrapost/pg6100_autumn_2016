package org.pg6100.soap.wiremock;

import com.nasdaqdod.ws.services.v1.NASDAQQuotes;
import com.nasdaqdod.ws.services.v1.NASDAQQuotesSoap;

import javax.xml.ws.BindingProvider;

public class WsProvider {

    public static final String NASDAQ_ADDRESS = "ws.nasdaqdod.com";

    public static NASDAQQuotesSoap getNASDAQQuotesSoap() {
        //use property, so we can change it during testing to point to WireMock
        String nasdaq = System.getProperty("nasdaqAddress", NASDAQ_ADDRESS);
        return getNASDAQQuotesSoap(nasdaq);
    }

    public static NASDAQQuotesSoap getNASDAQQuotesSoap(String webAddress){

        NASDAQQuotes service = new NASDAQQuotes();
        //"javax.xml.ws.service.endpoint.address" -> "http://ws.nasdaqdod.com/v1/NASDAQQuotes.asmx"
        String url = "http://"+webAddress+"/v1/NASDAQQuotes.asmx";

        NASDAQQuotesSoap ws = service.getNASDAQQuotesSoap();

        ((BindingProvider)ws).getRequestContext().put(
                BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);

        return ws;
    }
}
