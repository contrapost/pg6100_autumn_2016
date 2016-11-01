package org.pg6100.rest.newsrest.api;

import io.swagger.annotations.*;
import org.pg6100.news.constraint.Country;
import org.pg6100.rest.newsrest.dto.NewsDto;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;


@Api(value = "/news" , description = "Handling of creating and retrieving news")
@Path("/news")
@Produces({
        "application/vnd.pg6100.news+json; charset=UTF-8; version=1", //custom Json with versioning
        "application/json; charset=UTF-8" //old format
})
public interface NewsRestApi {

    //note: in interfaces, this is by default "public static final"
    String ID_PARAM ="The numeric id of the news";


    /*
        query parameters are in the form

        ? <name>=<value>

        for example

        /news?country=Norway&authordId=foo
     */

    @ApiOperation("Get all the news")
    @GET
    List<NewsDto> get(
            @ApiParam("The country name")
            @QueryParam("country")
                    String country,
            //
            @ApiParam("The id of the author who wrote the news")
            @QueryParam("authorId")
                    String authorId
    );


    @ApiOperation("Create a news")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiResponse(code = 200, message = "The id of newly created news")
    Long createNews(
            @ApiParam("Text of news, plus author id and country. Should not specify id or creation time")
                    NewsDto dto);

    @ApiOperation("Get a single news specified by id")
    @GET
    @Path("/{id}")
    NewsDto getNews(
            @ApiParam(ID_PARAM)
            @PathParam("id")
                    Long id);



    @ApiOperation("Update an existing news")
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    void updateNews(
            @ApiParam(ID_PARAM)
            @PathParam("id")
                    Long id,
            //
            @ApiParam("The news that will replace the old one. Cannot change its id though.")
                    NewsDto dto);

    @ApiOperation("Update the text content of an existing news")
    @PUT
    @Path("/{id}/text")
    @Consumes(MediaType.TEXT_PLAIN)
    void updateTextOfNews(
            @ApiParam(ID_PARAM)
            @PathParam("id")
                    Long id,
            //
            @ApiParam("The new text which will replace the old one")
                    String text
    );


    @ApiOperation("Delete a news with the given id")
    @DELETE
    @Path("/{id}")
    void deleteNews(
            @ApiParam(ID_PARAM)
            @PathParam("id")
                    Long id);


    /*
        The following, are all "deprecated" methods.
        A deprecated method is something that shouldn't be used,
        as it might be removed in a future release of the API.

        A typical version scheme is:

        X.Y.Z

        X = major version
        Y = minor version
        Z = patch

        Increasing Y or Z should NOT break backward compatibility.
        Increasing X "might" break backward compatibility.

        An approach for maintanability is to keep the old API, but
        then do automated permanent redirect (ie 301) to the new API.
        This only works if the new API is semantically equivalent
     */

    @ApiOperation("Get all the news in the specified country")
    @ApiResponses({
            @ApiResponse(code = 301, message = "Deprecated URI. Moved permanently.")
    })
    @GET
    @Path("/countries/{country}")
    @Deprecated
    Response deprecatedGetByCountry(
            @ApiParam("The country name")
            @PathParam("country")
            @Country String country);


    @ApiOperation("Get all the news written by the specified author")
    @ApiResponses({
            @ApiResponse(code = 301, message = "Deprecated URI. Moved permanently.")
    })
    @GET
    @Path("/authors/{author}")
    @Deprecated
    Response deprecatedGetByAuthor(
            @ApiParam("The id of the author who wrote the news")
            @PathParam("author")
            String author);


    @ApiOperation("Get all the news from a given country written by a given author")
    @ApiResponses({
            @ApiResponse(code = 301, message = "Deprecated URI. Moved permanently.")
    })
    @GET
    @Path("/countries/{country}/authors/{author}")
    @Deprecated
    Response deprecatedGetByCountryAndAuthor(
            @ApiParam("The country name")
            @PathParam("country")
            @Country String country,
            //
            @ApiParam("The id of the author who wrote the news")
            @PathParam("author")
            String author);



    @ApiOperation("Get a single news specified by id")
    @ApiResponses({
            @ApiResponse(code = 301, message = "Deprecated URI. Moved permanently.")
    })
    @GET
    @Path("/id/{id}")
    @Deprecated
    Response deprecatedGetById(
            @ApiParam(ID_PARAM)
            @PathParam("id")
            Long id);



    @ApiOperation("Update an existing news")
    @ApiResponses({
            @ApiResponse(code = 301, message = "Deprecated URI. Moved permanently.")
    })
    @PUT
    @Path("/id/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Deprecated
    Response deprecatedUpdate(
            @ApiParam(ID_PARAM)
            @PathParam("id")
            Long id,
            //
            @ApiParam("The news that will replace the old one. Cannot change its id though.")
            NewsDto dto);

    @ApiOperation("Update the text content of an existing news")
    @ApiResponses({
            @ApiResponse(code = 301, message = "Deprecated URI. Moved permanently.")
    })
    @PUT
    @Path("/id/{id}/text")
    @Consumes(MediaType.TEXT_PLAIN)
    @Deprecated
    Response deprecatedUpdateText(
            @ApiParam(ID_PARAM)
            @PathParam("id")
            Long id,
            //
            @ApiParam("The new text which will replace the old one")
            String text
            );


    @ApiOperation("Delete a news with the given id")
    @ApiResponses({
            @ApiResponse(code = 301, message = "Deprecated URI. Moved permanently.")
    })
    @DELETE
    @Path("/id/{id}")
    @Deprecated
    Response deprecatedDelete(
            @ApiParam(ID_PARAM)
            @PathParam("id")
            Long id);
}
