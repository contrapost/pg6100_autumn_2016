package org.pg6100.rest.restweb;

import java.io.Serializable;

public class Location implements Serializable {

    private String city;
    private String nation;

    public Location(String city, String nation) {
        this.city = city;
        this.nation = nation;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }
}
