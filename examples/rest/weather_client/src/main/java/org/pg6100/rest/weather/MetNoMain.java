package org.pg6100.rest.weather;

/*
    A list of Web Services

    http://www.programmableweb.com/


    Example: Weather forecast

    http://www.yr.no/

    http://api.met.no/weatherapi/documentation

    http://api.met.no/weatherapi/textforecast/1.6/schema
 */

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class MetNoMain {

    public static void main(String[] args){

        /*
            We use JAX-RS to access/create RESTful web services.

            REST =  Representational State Transfer

            JAX-RS is just a spec. Different implementations are for example
            RestEasy (from JBoss/Wildfly) and Jersey.
            This is the same concept of Hibernate being just an implementation of JPA.
         */


        //http://api.met.no/weatherapi/textforecast/1.6/?forecast=land;language=nb
        URI uri = UriBuilder.fromUri("http://api.met.no/weatherapi/textforecast/1.6")
                .port(80) // not necessary, as 80 is default anywat
                .queryParam("forecast","land") // equivalent to "?forecast=land"
                .queryParam("language","nb")   // equivalent to "&language=nb"
                .build();

        Client client = ClientBuilder.newClient();
        Response response = client.target(uri).request("application/xml").get();

        System.out.println("Result as string : " + response.readEntity(String.class));

        /*
        System.out.println("\n\nJAXB\n\n");

        //direct unmarshalling. does not work due wrong Schema though
        Weather weather = response.readEntity(Weather.class);

        System.out.println("Periods");
        for(Time time : weather.getTime()){
            System.out.println("From " + time.getVfrom() + " to " + time.getVto());
        }
        */
    }

}
