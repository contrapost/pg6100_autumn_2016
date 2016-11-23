package org.pg6100.rest.pagination;


import io.swagger.jaxrs.config.BeanConfig;


import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


//this defines the entry point of REST definitions. Can be only one.
@ApplicationPath("/api")
public class ApplicationConfig extends Application {


  private final Set<Class<?>> classes;


  public ApplicationConfig() {

    BeanConfig beanConfig = new BeanConfig();
    beanConfig.setVersion("0.0.1");
    beanConfig.setSchemes(new String[]{"http"});
    beanConfig.setBasePath("/pagination/api");
    beanConfig.setResourcePackage("org.pg6100.rest.pagination");

    //AWFUL NAME: this "set" is the one does actually init Swagger...
    beanConfig.setScan(true);

    /*
      Here we define which classes provide REST APIs
     */
    HashSet<Class<?>> c = new HashSet<>();
    c.add(PaginationRest.class);

    //add further configuration to activate SWAGGER
    c.add(io.swagger.jaxrs.listing.ApiListingResource.class);
    c.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);

    classes = Collections.unmodifiableSet(c);
  }

  @Override
  public Set<Class<?>> getClasses() {
    return classes;
  }

}