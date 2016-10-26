package org.pg6100.rest.restweb;


import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@SessionScoped
public class JsfBean implements Serializable{

    @EJB
    private LocationProvider locationProvider;

    public List<Location> getLocations(){
        return locationProvider.getLocations();
    }
}
