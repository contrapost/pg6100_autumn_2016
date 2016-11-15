package org.pg6100.rest.swarm;


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

    HashSet<Class<?>> c = new HashSet<>();
    c.add(SwarmRest.class);

    classes = Collections.unmodifiableSet(c);
  }

  @Override
  public Set<Class<?>> getClasses() {
    return classes;
  }

}