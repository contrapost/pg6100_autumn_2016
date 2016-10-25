package org.pg6100.rest.newsrest.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
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

    //note: in interfaces, this is by default "public static final"
    String ID_PARAM ="The numeric id of the news";


    @ApiOperation("Get all the news")
    @GET
    List<NewsDto> get();

    @ApiOperation("Get all the news in the specified country")
    @GET
    @Path("/countries/{country}")
    List<NewsDto> getByCountry(
            @ApiParam("The country name")
            @PathParam("country")
            @Country String country);


    @ApiOperation("Get all the news written by the specified author")
    @GET
    @Path("/authors/{author}")
    List<NewsDto> getByAuthor(
            @ApiParam("The id of the author who wrote the news")
            @PathParam("author")
            String author);


    @ApiOperation("Get all the news from a given country written by a given author")
    @GET
    @Path("/countries/{country}/authors/{author}")
    List<NewsDto> getByCountryAndAuthor(
            @ApiParam("The country name")
            @PathParam("country")
            @Country String country,
            //
            @ApiParam("The id of the author who wrote the news")
            @PathParam("author")
            String author);


    @ApiOperation("Create a news")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiResponse(code = 200, message = "The id of newly created news")
    Long createNews(
            @ApiParam("Text of news, plus author id and country. Should not specify id or creation time")
            NewsDto dto);



    @ApiOperation("Get a single news specified by id")
    @GET
    @Path("/id/{id}")
    NewsDto getById(
            @ApiParam(ID_PARAM)
            @PathParam("id")
            Long id);


    @ApiOperation("Update the content of an existing news")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    void update(
            @ApiParam("Id and new text for the existing news")
            NewsDto dto);



    @ApiOperation("Update the content of an existing news")
    @PUT
    @Path("/id/{id}")
    @Consumes(MediaType.TEXT_PLAIN)
    void update(
            @ApiParam(ID_PARAM)
            @PathParam("id")
            Long id,
            //
            @ApiParam("The new text which will replace the old one")
            String text
            );


    @ApiOperation("Delete a news with the given id")
    @DELETE
    @Path("/id/{id}")
    void delete(
            @ApiParam(ID_PARAM)
            @PathParam("id")
            Long id);
}
