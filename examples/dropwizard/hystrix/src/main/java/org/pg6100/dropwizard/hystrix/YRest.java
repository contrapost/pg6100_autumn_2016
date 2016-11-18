package org.pg6100.dropwizard.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import rx.Observable;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Created by arcuri82 on 18-Nov-16.
 */
@Path("y")
public class YRest {

    private final UriBuilder base;
    private final Client client;

    public YRest() {
        /*
            Note: here for simplicity I am calling another service in the
            same running Dropwizard (ie Jetty).
            This is just to avoid having to handle two independent
            instances running on two different JVM processes
         */
        base = UriBuilder.fromUri("http://localhost:8080/api/x");
        client = ClientBuilder.newClient();
    }


    @Path("single")
    @GET
    public long doGetSingle(
            @DefaultValue("30")
            @QueryParam("v")
                    long v) {

        /*
            this is synchronous, but would still give an answer within the given time (or
            immediately if the circuit breaker is on)
         */
        long result = new CallX(v).execute();
        return result;
    }

    @Path("multi")
    @GET
    public long doGetMulti(
            @DefaultValue("30") @QueryParam("a") long a,
            @DefaultValue("30") @QueryParam("b") long b,
            @DefaultValue("30") @QueryParam("c") long c,
            @DefaultValue("30") @QueryParam("d") long d,
            @DefaultValue("30") @QueryParam("e") long e
    ) {

        long result = Observable.merge(
                new CallX(a).observe(), // make these 5 calls in parallel,
                new CallX(b).observe(), // and asynchronously
                new CallX(c).observe(),
                new CallX(d).observe(),
                new CallX(e).observe()
        ).toList().toBlocking().single() // collect the results into a list
                .stream()
                .mapToLong(l -> l)
                .sum(); // sum all the returned values in the list

        return result;
    }


    /*
        Calls to an external web service will be wrapped into a HystrixCommand
     */
    private class CallX extends HystrixCommand<Long> {

        private final long x;

        protected CallX(long x) {
            super(HystrixCommandGroupKey.Factory.asKey("Interactions with X"));
            this.x = x;
        }

        @Override
        protected Long run() throws Exception {

            /*
                Note: this synchronous call could fail (and so throw an exception),
                or even just taking a long while (if server is under heavy load)
             */
            URI uri = base.build();
            long result = client.target(uri).request()
                    .post(Entity.text(x))
                    .readEntity(Long.class);

            return result;
        }

        @Override
        protected Long getFallback() {
            //this is what is returned in case of exceptions or timeouts
            return 0L;
        }
    }
}
