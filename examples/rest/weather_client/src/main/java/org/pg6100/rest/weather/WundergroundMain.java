package org.pg6100.rest.weather;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class WundergroundMain {

    //http://www.wunderground.com/

    public static void main(String[] args){

        //http://api.wunderground.com/api/302f56c7ad8e8c82/geolookup/conditions/forecast/q/Norway/Oslo.xml

        /*
            Need to register. Linked to a user.
            Up to 500 calls per day, 10 per minute.
            To have more, you need to pay.
         */
        String code = "302f56c7ad8e8c82";
        String country = "Norway";
        String city = "Oslo";

        //parameters passed as path elements, and a not as "?name=value" parameters
        URI uri = UriBuilder.fromUri("http://api.wunderground.com/api/"+code+"/geolookup/conditions/forecast/q/"+country+"/"+city+".json").port(80).build();

        Client client = ClientBuilder.newClient();
        Response response = client.target(uri).request("application/json").get();

        String result = response.readEntity(String.class);
        System.out.println("Result as string : " + result);


        /*
            Missing XSD:

            https://apicommunity.wunderground.com/weatherapi/topics/is_there_an_xsd_document_available_to_create_an_object_model_from_for_deserialization_in_c

            so cannot automatically generate Java classes for binding/conversion

            No documentation, just list of field names:

            http://www.wunderground.com/weather/api/d/docs?d=data/conditions&MR=1

            could manually write a POJO, and then map/translate with GSon, but quite large, see w_example.json
         */

        //just extract one element of interest
        JsonParser parser = new JsonParser();
        JsonObject json =(JsonObject) parser.parse(result);
        String temperature = json.get("current_observation").getAsJsonObject().get("temp_c").getAsString();

        System.out.println("Temperature in Oslo: "+temperature+" C'");
    }
}
