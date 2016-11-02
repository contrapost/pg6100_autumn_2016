package org.pg6100.soap.news.api;


import org.junit.BeforeClass;
import org.junit.Test;
import org.pg6100.soap.client.*;

import javax.xml.ws.BindingProvider;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by arcuri82 on 11/2/2016.
 */
public class NewsSoapApiIT {

    @Test
    public void foo(){}

    private static NewsSoap ws;

    @BeforeClass
    public static void initClass() {
        NewsSoapImplService service = new NewsSoapImplService();
        ws = service.getNewsSoapImplPort();

        String url = "http://localhost:8080/newssoap/NewsSoapImpl";

        ((BindingProvider)ws).getRequestContext().put(
                BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
    }


    @Test
    public void testCleanDB() {

        List<NewsDto> list = ws.get(null, null);

        assertEquals(0, list.size());
    }
}