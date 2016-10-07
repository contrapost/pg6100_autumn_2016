package org.pg6100.soap.wiremock;

import com.nasdaqdod.ws.services.v1.MarketCenter;
import com.nasdaqdod.ws.services.v1.NASDAQQuotesSoap;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Named
@SessionScoped
public class NasdaqBean implements Serializable {

    private List<String> names;

    public List<String> getNames() {
        if (names == null) {
            NASDAQQuotesSoap soap = WsProvider.getNASDAQQuotesSoap();
            names = soap.listMarketCenters().getMarketCenter().stream()
                    .map(MarketCenter::getName).collect(Collectors.toList());
        }
        return names;
    }
}
