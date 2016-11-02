package org.pg6100.rest.newsrest.api;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import io.swagger.annotations.ApiParam;
import org.pg6100.news.NewsEJB;
import org.pg6100.rest.newsrest.dto.NewsConverter;
import org.pg6100.rest.newsrest.dto.NewsDto;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.List;

/*
    The actual implementation could be a EJB, eg if we want to handle
    transactions and dependency injections with @EJB.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) //avoid creating new transactions
public class NewsRestImpl implements NewsRestApi {

    @EJB
    private NewsEJB ejb;


    @Override
    public List<NewsDto> get(String country, String authorId) {

        if (Strings.isNullOrEmpty(country) && Strings.isNullOrEmpty(authorId)) {
            return NewsConverter.transform(ejb.getAll());
        } else if (!Strings.isNullOrEmpty(country) && !Strings.isNullOrEmpty(authorId)) {
            return NewsConverter.transform(ejb.getAllByCountryAndAuthor(country, authorId));
        } else if (!Strings.isNullOrEmpty(country)) {
            return NewsConverter.transform(ejb.getAllByCountry(country));
        } else {
            return NewsConverter.transform(ejb.getAllByAuthor(authorId));
        }
    }

    @Override
    public Long createNews(NewsDto dto) {

        if (!(Strings.isNullOrEmpty(dto.newsId)
                && Strings.isNullOrEmpty(dto.id))) {
            throw new WebApplicationException("Cannot specify id for a newly generated news", 400);
        }
        if (dto.creationTime != null) {
            throw new WebApplicationException("Cannot specify creationTime for a newly generated news", 400);
        }

        Long id;
        try {
            id = ejb.createNews(dto.authorId, dto.text, dto.country);
        } catch (Exception e) {
            throw wrapException(e);
        }

        return id;
    }

    @Override
    public NewsDto getNews(Long id) {
        requireNewsExists(id);
        return NewsConverter.transform(ejb.get(id));
    }

    @Override
    public void updateNews(Long id, NewsDto dto) {
        long dtoID;
        try {
            dtoID = Long.parseLong(getNewsId(dto));
        } catch (Exception e) {
            throw new WebApplicationException("Invalid id: " + getNewsId(dto), 400);
        }

        if (dtoID != id) {
            // in this case, 409 (Conflict) sounds more appropriate than the generic 400
            throw new WebApplicationException("Not allowed to change the id of the resource", 409);
        }

        if (!ejb.isPresent(dtoID)) {
            throw new WebApplicationException("Not allowed to create a news with PUT, and cannot find news with id: " + dtoID, 404);
        }

        try {
            ejb.update(dtoID, dto.text, dto.authorId, dto.country, dto.creationTime);
        } catch (Exception e) {
            throw wrapException(e);
        }
    }

    @Override
    public void updateTextOfNews(Long id, String text) {
        requireNewsExists(id);

        try {
            ejb.updateText(id, text);
        } catch (Exception e) {
            throw wrapException(e);
        }
    }

    @Override
    public void deleteNews(@ApiParam(ID_PARAM) Long id) {
        /*
            Bit tricy: once a resource is deleted, if you try to
            delete it a second time, then you would expect a 404,
            ie resource not found.
            However, DELETE is idempotent, which might be confusing
            in this context. Doing 1 or 100 deletes on the server
            will not alter the result on the server (the resource is
            deleted), although the return values (204 vs 404) can
            be different (this does not affect the definition of
            idempotent)
         */
        requireNewsExists(id);

        ejb.deleteNews(id);
    }


    // deprecated methods --------------------------------------------------------------

    @Override
    public Response deprecatedGetByCountry(String country) {
        return Response.status(301)
                .location(UriBuilder.fromUri("news")
                        .queryParam("country", country).build())
                .build();
    }


    @Override
    public Response deprecatedGetByAuthor(String author) {
        return Response.status(301)
                .location(UriBuilder.fromUri("news")
                        .queryParam("authorId", author).build())
                .build();
    }


    @Override
    public Response deprecatedGetByCountryAndAuthor(String country, String author) {
        return Response.status(301)
                .location(UriBuilder.fromUri("news")
                        .queryParam("country", country)
                        .queryParam("authorId", author).build())
                .build();
    }

    @Override
    public Response deprecatedGetById(Long id) {
        return Response.status(301)
                .location(UriBuilder.fromUri("news/" + id).build())
                .build();
    }

    @Override
    public Response deprecatedUpdate(Long id, NewsDto dto) {
        return Response.status(301)
                .location(UriBuilder.fromUri("news/" + id).build())
                .build();
    }

    @Override
    public Response deprecatedUpdateText(Long id, String text) {
        return Response.status(301)
                .location(UriBuilder.fromUri("news/" + id + "/text").build())
                .build();
    }

    @Override
    public Response deprecatedDelete(Long id) {
        return Response.status(301)
                .location(UriBuilder.fromUri("news/" + id).build())
                .build();
    }


    //----------------------------------------------------------

    /**
     * Code used to keep backward compatibility
     *
     * @param dto
     * @return
     */
    private String getNewsId(NewsDto dto){

        if(dto.newsId != null){
            return dto.newsId;
        } else {
            return dto.id;
        }
    }

    private void requireNewsExists(long id) throws WebApplicationException {
        if (!ejb.isPresent(id)) {
            throw new WebApplicationException("Cannot find news with id: " + id, 404);
        }
    }

    private WebApplicationException wrapException(Exception e) throws WebApplicationException {

        Throwable cause = Throwables.getRootCause(e);
        if (cause instanceof ConstraintViolationException) {
            return new WebApplicationException("Invalid constraints on input: " + cause.getMessage(), 400);
        } else {
            return new WebApplicationException("Internal error", 500);
        }
    }
}
