package org.pg6100.rest.pagination;


import org.pg6100.rest.pagination.dto.base.NewsDto;
import org.pg6100.rest.pagination.dto.collection.ListDto;
import org.pg6100.rest.pagination.dto.hal.HalLink;
import org.pg6100.rest.pagination.jee.NewsEJB;
import org.pg6100.rest.pagination.jee.entity.News;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path("/")
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class PaginationRest {

    /*
        As there is only a fixed number of values for "expand",
        I am going to use an enumeration.
     */
    public enum Expand {
        ALL,
        NONE,
        COMMENTS,
        VOTES;

        public boolean isWithComments() {
            return this.equals(COMMENTS) || this.equals(ALL);
        }

        public boolean isWithVotes() {
            return this.equals(VOTES) || this.equals(ALL);
        }
    }

    /*
        Injected to get information about the URL of the running Wildfly.
        This is needed to create full URL links to the different resources.
     */
    @Context
    UriInfo uriInfo;

    @EJB
    private NewsEJB ejb;


    @GET
    @Path("/news")
    @Produces(Format.HAL_V1)
    public ListDto<NewsDto> getNews(
            @QueryParam("country")
                    String country,
            @QueryParam("offset")
            @DefaultValue("0")
                    Integer offset,
            @QueryParam("limit")
            @DefaultValue("10")
                    Integer limit,
            @QueryParam("expand")
            @DefaultValue("NONE")
                    Expand expand
    ) {

        if(offset < 0){
            throw new WebApplicationException("Negative offset: "+offset, 400);
        }

        if(limit < 1){
            throw new WebApplicationException("Limit should be at least 1: "+limit, 400);
        }

        /*
            It is generally a really bad idea to read the whole database,
            and even worse make it available from a REST API.
            So, have a max number of entries that will be returned.

            For example, when you search for projects on Github (and they
            do also have REST APIs for it), there is a limit of 1000 results.
         */
        int maxFromDb = 50;

        /*
            Based on the "expand" parameter, I will decide whether to load
            or not the votes and comments from the database
         */

        List<News> newsList;
        switch (expand) {
            case ALL:
                newsList = ejb.getNewsList(country, true, true, maxFromDb);
                break;
            case NONE:
                newsList = ejb.getNewsList(country, false, false, maxFromDb);
                break;
            case COMMENTS:
                newsList = ejb.getNewsList(country, true, false, maxFromDb);
                break;
            case VOTES:
                newsList = ejb.getNewsList(country, false, true, maxFromDb);
                break;
            default:
                /*
                    This is actually a server error 5xx.
                    This can happen if, later on, the enum Expand gets new
                    fields, but we forgot to handle them here
                 */
                throw new WebApplicationException("Unhandled case: " + expand, 500);
        }

        /*
            Note: we could cache the results of the JPQL query.
            This is tricky:
            - an internal cache would not work if the load balancer redirect following
              requests to a different running instance
            - caches are inefficient if most of the users will just read the first page
            - if no cache, then there is no guarantee that the data has not been changed
              meanwhile when asking for the "next" page
            - could write the results of JPQL query to disk, but then very inefficient
              if users ask only for one page (plus need to make sure to delete such query
              results after a period of time)

            There is no best solution: all have positive and negative sides.
            When dealing with performance, you need to keep in mind the expected
            usage of the API.
            Here, to make it simple, we simply do not cache the query.
            So, following a "next" link would result in a new database query.
         */

        if(offset != 0 && offset >=  newsList.size()){
            throw new WebApplicationException("Offset "+ offset + " out of bound "+newsList.size(), 400);
        }

        ListDto<NewsDto> dto = DtoTransformer.transform(
                newsList, offset, limit, expand.isWithComments(), expand.isWithVotes());

        UriBuilder builder = uriInfo.getBaseUriBuilder()
                .path("/news")
                .queryParam("expand", expand)
                .queryParam("limit", limit);

        if(country != null){
            builder = builder.queryParam("country", country);
        }

        /*
            Create URL links for "self", "next" and "previous" pages.
            Each page will have up to "limit" NewsDto objects.
            A page is identified by the offset in the list.

            Note: needs to clone the builder, as each call
            like "queryParam" does not create a new one, but
            rather update the existing one
         */

        dto._links.self = new HalLink(builder.clone()
                .queryParam("offset", offset)
                .build().toString()
        );

        if (!newsList.isEmpty() && offset > 0) {
            dto._links.previous = new HalLink(builder.clone()
                    .queryParam("offset", Math.max(offset - limit, 0))
                    .build().toString()
            );
        }
        if (offset + limit < newsList.size()) {
            dto._links.next = new HalLink(builder.clone()
                    .queryParam("offset", offset + limit)
                    .build().toString()
            );
        }

        return dto;
    }

    @DELETE
    @Path("/news/{id}")
    public void deleteNews(@PathParam("id") Long newsId){
        ejb.deleteNews(newsId);
    }

    @GET
    @Path("/news/{id}")
    @Produces(Format.JSON_V1)
    public NewsDto getNews(@PathParam("id") Long newsId){
        News news = ejb.getNews(newsId);
        if(news == null){
            throw new WebApplicationException(404);
        }
        return DtoTransformer.transform(news, true, true);
    }


    @POST
    @Path("/news")
    @Consumes(Format.JSON_V1)
    public Response createNews(NewsDto dto) {

        long id = ejb.createNews(dto.text, dto.country);
        return Response.created(
                uriInfo.getBaseUriBuilder()
                        .path("/news/" + id)
                        .build()
        ).build();
    }

    @POST
    @Path("/news/{id}/votes")
    @Consumes(Format.JSON_V1)
    public Response createVote(
            @PathParam("id")
                    Long newsId,
            String user) {

        ejb.createVote(newsId, user);

        return Response.status(201).build();
    }

    @POST
    @Path("/news/{id}/comments")
    @Consumes(Format.JSON_V1)
    public Response createComment(
            @PathParam("id")
                    Long newsId,
            String text) {

        ejb.createComment(newsId, text);

        return Response.status(201).build();
    }

}
