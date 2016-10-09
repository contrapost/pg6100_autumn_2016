package org.pg6100.rest.restweb;


import javax.ejb.EJB;
import javax.faces.bean.RequestScoped;
import javax.inject.Named;
import java.util.List;

@Named
@RequestScoped
public class JsfBean {

    @EJB
    private LocationProvider locationProvider;

    public List<Location> getLocations(){
        return locationProvider.getLocations();
    }
}
