package org.pg6100.rest.restweb;


import javax.faces.bean.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
@RequestScoped
public class JsfBean {

    @Inject
    private LocationProvider locationProvider;

    public List<Location> getLocations(){
        return locationProvider.getLocations();
    }
}
