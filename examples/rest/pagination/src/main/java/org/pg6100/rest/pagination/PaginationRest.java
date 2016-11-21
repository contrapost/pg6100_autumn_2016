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
import javax.validation.constraints.Min;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path("/")
@Produces(Format.JSON_V1)
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class PaginationRest {

    public enum Expand {
        ALL,
        NONE,
        COMMENTS,
        VOTES;

        public boolean withComments() {
            return this.equals(COMMENTS) || this.equals(ALL);
        }

        public boolean withVotes() {
            return this.equals(VOTES) || this.equals(ALL);
        }
    }

    @Context
    UriInfo uriInfo;

    @EJB
    private NewsEJB ejb;


    @GET
    @Path("/news")
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

        int maxFromDb = 50;

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
            TODO explanation
         */

        ListDto<NewsDto> dto = DtoTransformer.transform(
                newsList, offset, limit, expand.withComments(), expand.withVotes());

        UriBuilder builder = uriInfo.getBaseUriBuilder()
                .path("/news")
                .queryParam("expand", expand)
                .queryParam("limit", limit);

        if(country != null){
            builder = builder.queryParam("country", country);
        }

        /*
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


        //TODO fix Content-Range

        return dto;
    }

    @DELETE
    @Path("/news/{id}")
    public void deleteNews(@PathParam("id") Long newsId){
        ejb.deleteNews(newsId);
    }

    @GET
    @Path("/news/{id}")
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
