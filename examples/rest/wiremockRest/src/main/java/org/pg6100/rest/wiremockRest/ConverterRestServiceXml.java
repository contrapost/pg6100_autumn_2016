package org.pg6100.rest.wiremockRest;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

@Path("/convert")
@Stateless
public class ConverterRestServiceXml {

    private final String webAddress;

    public ConverterRestServiceXml() {

        /*
             Important that we use a system property (which can be set with
             -D from command line).
             Reason? we ll change the server when running tests, as to be
             able to mock responses deterministically
         */
        webAddress = System.getProperty("fixerWebAddress", "api.fixer.io");
    }


    @GET
    @Produces(MediaType.APPLICATION_XML)
    public ConversionDTO getExchangeRate(@QueryParam("from") String from, @QueryParam("to") String to) throws ClientErrorException, ServerErrorException {

        if (Strings.isNullOrEmpty(from) || Strings.isNullOrEmpty(to)) {
            throw new WebApplicationException("Missing parameters", 400);
        }

        //call an external web service to get the rate
        double val = getConversion(from, to);

        //convert such "rate" into our own dto format
        ConversionDTO dto = new ConversionDTO(from, to, val);

        return dto;
    }


    private double getConversion(String from, String to) throws ClientErrorException, ServerErrorException {

        //example:  http://api.fixer.io/latest?base=NOK
        URI uri = UriBuilder.fromUri("http://" + webAddress + "/latest").queryParam("base", from).build();

        Client client = ClientBuilder.newClient();
        Response response = client.target(uri).request(MediaType.APPLICATION_JSON_TYPE).get();

        /*
            Here the choice of error family (4xx or 5xx?) is tricky.

            If user sends wrong inputs, then of course we return a 4xx.
            But what if the external service (http://api.fixer.io in this case)
            is down? It is neither our fault (I mean this Rest service), nor of the
            client user. Still, we need to return a 5xx, as the request was correct
            (so no 4xx), but the service could not fulfil it.

             So, in general, a 5xx could mean either:
             (1) a bug in the service
             (2) service depends on external services (or database) that are
                 not working properly.
         */


        Response.StatusType st = response.getStatusInfo();

        if (st.getFamily().equals(Response.Status.Family.CLIENT_ERROR)) {
            throw new ClientErrorException(st.getStatusCode());
        }
        if (st.getFamily().equals(Response.Status.Family.SERVER_ERROR)) {
            throw new ServerErrorException(st.getStatusCode());
        }

        ObjectMapper jackson = new ObjectMapper();

        JsonNode rootNode;
        try {
            String json = response.readEntity(String.class);
            rootNode = jackson.readValue(json, JsonNode.class);
        } catch (Exception e) {
            throw new WebApplicationException("Invalid JSON data as input: " + e.getMessage(), 400);
        }

        String base = rootNode.get("base").asText();

        if (!from.equals(base) || ! rootNode.has("rates")) {
            throw new WebApplicationException("Internal error", 500);
        }

        if(! rootNode.get("rates").has(to)){
            throw new WebApplicationException("Non-recognized: "+to, 404);
        }

        try {
            return rootNode.get("rates").get(to).asDouble();
        } catch (Exception e){
            throw new WebApplicationException("Internal error", 500);
        }
    }
}
