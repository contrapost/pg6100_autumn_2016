package org.pg6100.rest.newsrest.api;

import com.google.common.base.Throwables;
import org.pg6100.news.NewsEJB;
import org.pg6100.rest.newsrest.dto.NewsConverter;
import org.pg6100.rest.newsrest.dto.NewsDto;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.WebApplicationException;
import java.util.List;

/*
    The actual implementation could be a EJB, eg if we want to handle
    transactions and dependency injections with @EJB.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) //avoid creating new transactions
public class NewsRestImpl implements NewsRestApi{

    @EJB
    private NewsEJB ejb;


    @Override
    public List<NewsDto> get() {
        return NewsConverter.transform(ejb.getAll());
    }

    @Override
    public List<NewsDto> getByCountry(String country) {
        return NewsConverter.transform(ejb.getAllByCountry(country));
    }


    @Override
    public List<NewsDto> getByAuthor(String author) {
        return NewsConverter.transform(ejb.getAllByAuthor(author));
    }


    @Override
    public List<NewsDto> getByCountryAndAuthor(String country, String author) {
        return NewsConverter.transform(ejb.getAllByCountryAndAuthor(country, author));
    }

    @Override
    public Long createNews(NewsDto dto) {

        /*
            Error code 400:
            the user had done something wrong, eg sent invalid input configurations
         */

        if(dto.id != null){
            throw new WebApplicationException("Cannot specify id for a newly generated news", 400);
        }
        if(dto.creationTime != null){
            throw new WebApplicationException("Cannot specify creationTime for a newly generated news", 400);
        }

        Long id;
        try{
            id = ejb.createNews(dto.authorId, dto.text, dto.country);
        }catch (Exception e){
            /*
                note: this work just because NOT_SUPPORTED,
                otherwise a rolledback transaction would propagate to the
                caller of this method
             */
            throw wrapException(e);
        }

        return id;
    }

    @Override
    public NewsDto getById(Long id) {
        return NewsConverter.transform(ejb.get(id));
    }

    @Override
    public void update(Long pathId, NewsDto dto) {
        long id;
        try{
            id = Long.parseLong(dto.id);
        } catch (Exception e){
            throw new WebApplicationException("Invalid id: "+dto.id, 400);
        }

        if(id != pathId){
            // in this case, 409 (Conflict) sounds more appropriate than the generic 400
            throw new WebApplicationException("Now allowed to change the id of the resource", 409);
        }

        if(! ejb.isPresent(id)){
            throw new WebApplicationException("Not allowed to create a news with PUT, and cannot find news with id: "+id, 404);
        }

        try {
            ejb.update(id, dto.text, dto.authorId, dto.country, dto.creationTime);
        } catch (Exception e){
            throw wrapException(e);
        }
    }

    @Override
    public void updateText(Long id, String text) {
        if(! ejb.isPresent(id)){
            throw new WebApplicationException("Cannot find news with id: "+id, 404);
        }

        try {
            ejb.updateText(id, text);
        } catch (Exception e){
            throw wrapException(e);
        }
    }

    @Override
    public void delete(Long id) {
        ejb.deleteNews(id);
    }


    //----------------------------------------------------------

    private WebApplicationException wrapException(Exception e) throws WebApplicationException{

        /*
            Errors:
            4xx: the user has done something wrong, eg asking for something that does not exist (404)
            5xx: internal server error (eg, could be a bug in the code)
         */

        Throwable cause = Throwables.getRootCause(e);
        if(cause instanceof ConstraintViolationException){
            return new WebApplicationException("Invalid constraints on input: "+cause.getMessage(), 400);
        } else {
            return new WebApplicationException("Internal error", 500);
        }
    }
}
