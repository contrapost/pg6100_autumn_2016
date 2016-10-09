package org.pg6100.rest.newsrest;


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
    HashSet<Class<?>> c = new HashSet<>();
    c.add(NewsRestImpl.class);
    c.add(CountryRest.class);

    classes = Collections.unmodifiableSet(c);
  }

  @Override
  public Set<Class<?>> getClasses() {
    return classes;
  }

}