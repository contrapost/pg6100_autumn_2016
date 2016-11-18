package org.pg6100.dropwizard.hystrix;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * Created by arcuri82 on 18-Nov-16.
 */
@Path("x")
public class XRest {

    @POST
    public Long doProcess(Long x) throws Exception{

        /*
            simulate some delay in the response, eg due to network congestion
            or hardware/software issues of the server
         */
        Thread.sleep(x);

        return x * 2;
    }
}
