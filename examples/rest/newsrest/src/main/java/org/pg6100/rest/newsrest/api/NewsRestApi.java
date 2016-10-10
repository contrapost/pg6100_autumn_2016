package org.pg6100.rest.newsrest.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.pg6100.news.constraint.Country;
import org.pg6100.rest.newsrest.dto.NewsDto;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Api(value = "/news" , description = "Handling of creating and retrieving news")
@Path("/news")
@Produces(MediaType.APPLICATION_JSON)
public interface NewsRestApi {

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
