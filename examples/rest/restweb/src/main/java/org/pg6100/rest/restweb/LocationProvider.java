package org.pg6100.rest.restweb;

import javax.ejb.Singleton;
import java.util.Arrays;
import java.util.List;

@Singleton
public class LocationProvider {

    public List<Location> getLocations(){
        return Arrays.asList(
                new Location("Oslo", "Norway"),
                new Location("London","UK"),
                new Location("Rome","Italy"),
                new Location("Dublin","Ireland"),
                new Location("Berlin","Germany")
        );
    }
}
