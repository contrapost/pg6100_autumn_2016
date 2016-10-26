package org.pg6100.rest.charset;


import io.swagger.annotations.ApiOperation;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/best")
public class BestDrink {


    @ApiOperation("Get the name of the best drink")
    @GET  // provide the resource as String in two different charset encodings
    @Produces({"text/plain;charset=UTF-8", "text/plain;charset=ISO-8859-1"})
    public String getTheBest(@HeaderParam("Accept-Language") String languages){

        String value;
        if(languages == null){
            value = "Beer";
        } else if(languages.trim().toLowerCase().equals("no")){
            /*
                actually, in the Accept-Language there can be a list of
                acceptable languages, and would need to parse it.
                but here for this example, it is not so important.
                see the specs if you want to see the details on how
                such string should be parsed
             */
            value = "Øl";
        } else {
            return "Beer";
        }

        return value;
    }
}
