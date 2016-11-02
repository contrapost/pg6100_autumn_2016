package org.pg6100.soap.news.api;

import com.google.common.base.Strings;
import org.pg6100.news.NewsEJB;
import org.pg6100.soap.news.dto.NewsConverter;
import org.pg6100.soap.news.dto.NewsDto;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.WebService;
import java.util.List;

/**
 * Created by arcuri82 on 11/2/2016.
 */
@WebService(
        endpointInterface = "org.pg6100.soap.news.api.NewsSoapApi"
//        portName = "http://localhost:8080/newssoap",
//        targetNamespace = "http://localhost:8080/newssoap",
//        serviceName = "NewsSoap"

)
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class NewsSoapImpl implements NewsSoapApi{

    //each public method will exported by default

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

//        if (!(Strings.isNullOrEmpty(dto.newsId)
//                && Strings.isNullOrEmpty(dto.id))) {
//            throw new WebApplicationException("Cannot specify id for a newly generated news", 400);
//        }
//        if (dto.creationTime != null) {
//            throw new WebApplicationException("Cannot specify creationTime for a newly generated news", 400);
//        }

        Long id;
        try {
            id = ejb.createNews(dto.authorId, dto.text, dto.country);
        } catch (Exception e) {
            //throw wrapException(e);
            throw e;
        }

        return id;
    }
}
