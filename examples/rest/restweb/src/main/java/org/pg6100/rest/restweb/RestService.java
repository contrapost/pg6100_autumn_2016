package org.pg6100.rest.restweb;

import com.google.gson.Gson;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Stateless
@Path("/locations")
public class RestService {

    @EJB
    private LocationProvider locationProvider;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLocations() {

        List<Location> locations = locationProvider.getLocations();

        Gson gson = new Gson();
        String json = gson.toJson(locations);
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }
}
