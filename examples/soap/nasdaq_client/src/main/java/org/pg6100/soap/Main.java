package org.pg6100.soap;


import com.nasdaqdod.ws.services.v1.MarketCenter;
import com.nasdaqdod.ws.services.v1.NASDAQQuotes;
import com.nasdaqdod.ws.services.v1.NASDAQQuotesSoap;

public class Main {

    public static void main(String[] args) throws Exception {

        //http://www.nasdaqdod.com/NASDAQQuotes.asmx?op=ListMarketCenters

        NASDAQQuotes service = new NASDAQQuotes();
        NASDAQQuotesSoap ws = service.getNASDAQQuotesSoap();

        for (MarketCenter mc : ws.listMarketCenters().getMarketCenter()) {
            System.out.println(mc.getName());
        }

        /*
        The NASDAQ Stock Market LLC
        CBOE Stock Exchange
        NASDAQ OMX BX, Inc
        Chicago Stock Exchange
        National Stock Exchange
        International Securities Exchange
        Industry Regulatory Authority
        BATS Exchange Inc.
        NYSE Arca
        NYSE Euronext
        NYSE AMEX
        Market Independent
        EDGA Exchange Inc.
        EDGX Exchange Inc.
        NASDAQ OMX PSX
        BATS Y-Exchange Inc.
        The NASDAQ Stock Market LLC
        OTC Bulletin Board
        OTC Markets Group
         */
    }
}
