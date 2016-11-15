package org.pg6100.rest.swarm;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Created by arcuri82 on 11/15/2016.
 */
@Path("/example")
public class SwarmRest {

    public static final String MESSAGE = "Wildfly Swarm Example";

    @GET
    public String getSwarmMessage(){
        return MESSAGE;
    }
}
