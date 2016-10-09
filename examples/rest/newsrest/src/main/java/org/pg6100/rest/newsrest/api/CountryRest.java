package org.pg6100.rest.newsrest.api;

import org.pg6100.news.country.CountryList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("country")
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public class CountryRest {

    @GET
    public List<String> get(){
        return CountryList.getCountries();
    }

}
