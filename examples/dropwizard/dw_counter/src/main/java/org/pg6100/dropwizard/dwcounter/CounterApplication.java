package org.pg6100.dropwizard.dwcounter;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;

/*
    Dropwizard uses:

    - Jetty: for servlet implementation
    - Jersey: for JAX-RS (in contrast to RestEasy in Wildfly)
    - Jackson: for Json un/marshaling (same as Wildfly)
 */
public class CounterApplication extends Application<CounterConfiguration> {


    public static void main(String[] args) throws Exception {
        new CounterApplication().run(args);
    }

    @Override
    public String getName() {
        return "Counter written in DropWizard";
    }


    @Override
    public void run(CounterConfiguration configuration, Environment environment) {

        environment.jersey().setUrlPattern("/patch/api/*");
        environment.jersey().register(new CounterRest());

        //swagger
        environment.jersey().register(new ApiListingResource());

        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("0.0.1");
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setHost("localhost:8080");
        beanConfig.setBasePath("/patch");
        beanConfig.setResourcePackage("org.pg6100.dropwizard.dwcounter");
        beanConfig.setScan(true);
    }

}