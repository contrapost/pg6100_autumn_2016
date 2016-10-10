package org.pg6100.rest.newsrest.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.pg6100.news.constraint.CountryList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Api(value = "/country", description = "API for country data.")
@Path("/country")
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public class CountryRest {

    @ApiOperation("Retrieve list of country names")
    @GET
    public List<String> get(){
        return CountryList.getCountries();
    }

}
