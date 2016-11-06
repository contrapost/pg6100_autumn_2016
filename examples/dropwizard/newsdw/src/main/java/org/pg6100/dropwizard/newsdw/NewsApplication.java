package org.pg6100.dropwizard.newsdw;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import org.pg6100.dropwizard.newsdw.api.NewsRestImpl;

/*
    Dropwizard uses:

    - Jetty: for servlet implementation
    - Jersey: for JAX-RS (in contrast to RestEasy in Wildfly)
    - Jackson: for Json un/marshaling (same as Wildfly)
 */
public class NewsApplication extends Application<NewsConfiguration> {


    public static void main(String[] args) throws Exception {
        new NewsApplication().run(args);
    }

    @Override
    public String getName() {
        return "Counter written in DropWizard";
    }

    @Override
    public void initialize(Bootstrap<NewsConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/assets", "/", null, "a"));
        bootstrap.addBundle(new AssetsBundle("/assets/css", "/css", null, "b"));
        bootstrap.addBundle(new AssetsBundle("/assets/fonts", "/fonts", null, "c"));
        bootstrap.addBundle(new AssetsBundle("/assets/images", "/images", null, "d"));
        bootstrap.addBundle(new AssetsBundle("/assets/lang", "/lang", null, "e"));
        bootstrap.addBundle(new AssetsBundle("/assets/lib", "/lib", null, "f"));
    }


    @Override
    public void run(NewsConfiguration configuration, Environment environment) {

        environment.jersey().setUrlPattern("/newsdw/api/*");
        environment.jersey().register(new NewsRestImpl());

        //swagger
        environment.jersey().register(new ApiListingResource());

        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("0.0.1");
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setHost("localhost:8080");
        beanConfig.setBasePath("/newsdw");
        beanConfig.setResourcePackage("org.pg6100.dropwizard.newsdw.api");
        beanConfig.setScan(true);

        //add further configuration to activate SWAGGER
        environment.jersey().register(new io.swagger.jaxrs.listing.ApiListingResource());
        environment.jersey().register(new io.swagger.jaxrs.listing.SwaggerSerializers());
    }

}