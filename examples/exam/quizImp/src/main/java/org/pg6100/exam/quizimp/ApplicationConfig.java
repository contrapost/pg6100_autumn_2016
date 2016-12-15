package org.pg6100.exam.quizimp;


import io.swagger.jaxrs.config.BeanConfig;
import org.pg6100.exam.quizimp.rest.CategoryRestImp;
import org.pg6100.exam.quizimp.rest.QuizRestImp;

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
    beanConfig.setVersion("1.0.0");
    beanConfig.setSchemes(new String[]{"http"});
    beanConfig.setHost("localhost:8080");
    beanConfig.setBasePath("/quiz/api");
    beanConfig.setResourcePackage("org.pg6100.exam.quizapi");
    beanConfig.setScan(true);

    HashSet<Class<?>> c = new HashSet<>();
    c.add(CategoryRestImp.class);
    c.add(QuizRestImp.class);

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