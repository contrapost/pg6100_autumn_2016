package org.pg6100.rest.newsrest.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.pg6100.news.constraint.Country;
import org.pg6100.rest.newsrest.dto.NewsDto;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/*
    When defining a REST API, there is going to be a lot of annotations that
    are required. To make things more readable, it is a good practice to
    have the API definition (with all the annotations) in an interface,
    and the actual internal logic in a concrete class.
 */


@Api(value = "/news" , description = "Handling of creating and retrieving news")
// when the url is "<base>/news", then this class will be used to handle it
@Path("/news")
@Produces(MediaType.APPLICATION_JSON) // states that, when a method returns something, it is in Json
public interface NewsRestApi {

    /*
        Main HTTP verbs/methods:
        GET: get the resource specified in the URL
        POST: send data, creating a new resource
        PUT: update a resource
        DELETE: delete the resource
     */

    /*
        Note: here inputs (what is in the method parameters) and outputs will
        be automatically processed by Wildfly using its own Json library, eg Jackson.
        So, when we have
        "List<NewsDto>"
        as return value, Wildfly will automatically marshall it into Json
     */


    @ApiOperation("Get all the news")
    @GET
    List<NewsDto> get();

    @GET
    @Path("/countries/{country}")
    List<NewsDto> getByCountry(@PathParam("country") @Country String country);


    @GET
    @Path("/authors/{author}")
    List<NewsDto> getByAuthor(@PathParam("author") String author);

    @GET
    @Path("/countries/{country}/authors/{author}")
    List<NewsDto> getByCountryAndAuthor(
            @PathParam("country") @Country String country,
            @PathParam("author") String author);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Long createNews(NewsDto dto);

    @GET
    @Path("/id/{id}")
    NewsDto getById(@PathParam("id") Long id);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    void update(NewsDto dto);

    @DELETE
    @Path("/id/{id}")
    void delete(@PathParam("id") Long id);
}
