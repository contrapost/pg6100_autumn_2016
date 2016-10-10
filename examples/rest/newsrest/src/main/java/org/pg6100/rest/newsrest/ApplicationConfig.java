package org.pg6100.rest.newsrest;


import io.swagger.jaxrs.config.BeanConfig;
import org.pg6100.rest.newsrest.api.CountryRest;
import org.pg6100.rest.newsrest.api.NewsRestImpl;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


@ApplicationPath("/api")
public class ApplicationConfig extends Application {


  private final Set<Class<?>> classes;


  public ApplicationConfig() {

    BeanConfig beanConfig = new BeanConfig();
    beanConfig.setVersion("0.0.1");
    beanConfig.setSchemes(new String[]{"http"});
    beanConfig.setHost("localhost:8080");
    beanConfig.setBasePath("/newsrest");
    //beanConfig.setFilterClass("io.swagger.sample.util.ApiAuthorizationFilterImpl");
    beanConfig.setResourcePackage("org.pg6100.rest.newsrest");

    //AWFUL NAME: this "set" is the one does actually init Swagger...
    beanConfig.setScan(true);

    HashSet<Class<?>> c = new HashSet<>();
    c.add(NewsRestImpl.class);
    c.add(CountryRest.class);

    c.add(io.swagger.jaxrs.listing.ApiListingResource.class);
    c.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);


    classes = Collections.unmodifiableSet(c);
  }

  @Override
  public Set<Class<?>> getClasses() {
    return classes;
  }

}