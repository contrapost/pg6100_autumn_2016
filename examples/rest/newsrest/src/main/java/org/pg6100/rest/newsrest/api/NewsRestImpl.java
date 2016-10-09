package org.pg6100.rest.newsrest.api;

import com.google.common.base.Throwables;
import org.pg6100.news.NewsEJB;
import org.pg6100.news.country.CountryList;
import org.pg6100.rest.newsrest.dto.NewsConverter;
import org.pg6100.rest.newsrest.dto.NewsDto;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.WebApplicationException;
import java.util.List;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class NewsRestImpl implements NewsRestApi{

    @EJB
    private NewsEJB ejb;


    @Override
    public List<NewsDto> get() {
        return NewsConverter.transform(ejb.getAll());
    }

    @Override
    public List<NewsDto> getByCountry(String country) {

        validateCountry(country);
        return NewsConverter.transform(ejb.getAllByCountry(country));
    }


    @Override
    public List<NewsDto> getByAuthor(String author) {
        return NewsConverter.transform(ejb.getAllByAuthor(author));
    }


    @Override
    public List<NewsDto> getByCountryAndAuthor(String country, String author) {

        validateCountry(country);
        return NewsConverter.transform(ejb.getAllByCountryAndAuthor(country, author));
    }

    @Override
    public Long createNews(NewsDto dto) {

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
            throw wrapException(e);
        }

        return id;
    }

    @Override
    public NewsDto getById(Long id) {
        return NewsConverter.transform(ejb.get(id));
    }

    @Override
    public void update(NewsDto dto) {
        try {
            ejb.updateText(Long.parseLong(dto.id), dto.text);
        } catch (Exception e){
            throw wrapException(e);
        }
    }

    @Override
    public void delete(Long id) {
        ejb.deleteNews(id);
    }


    //----------------------------------------------------------

    private void validateCountry(String country) throws WebApplicationException{
        if(!CountryList.isValidCountry(country)){
            throw new WebApplicationException("Invalid country: "+country, 400);
        }
    }

    private WebApplicationException wrapException(Exception e) throws WebApplicationException{
        Throwable cause = Throwables.getRootCause(e);
        if(cause instanceof ConstraintViolationException){
            return new WebApplicationException("Invalid constraints on input: "+cause.getMessage(), 400);
        } else {
            return new WebApplicationException("Internal error", 500);
        }
    }
}
