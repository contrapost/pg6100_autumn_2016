package org.pg6100.soap.news.api;

import com.google.common.base.Strings;
import org.pg6100.news.NewsEJB;
import org.pg6100.news.NewsEntity;
import org.pg6100.soap.news.dto.NewsConverter;
import org.pg6100.soap.news.dto.NewsDto;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.WebService;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Created by arcuri82 on 11/2/2016.
 */
@WebService(
        endpointInterface = "org.pg6100.soap.news.api.NewsSoapApi"
)
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class NewsSoapImpl implements NewsSoapApi{

    @EJB
    private NewsEJB ejb;

    /*
        Note: here in these methods I could have added
        more input validations...
     */

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

        Long id = ejb.createNews(dto.authorId, dto.text, dto.country);
        return id;
    }


    @Override
    public NewsDto getNews(Long id) {

        NewsEntity entity = ejb.get(id);
        if(entity ==null){
            return null;
        }

        return NewsConverter.transform(entity);
    }

    @Override
    public void updateNews(NewsDto dto) {

        ejb.update(dto.newsId,
                dto.text, dto.authorId, dto.country, ZonedDateTime.now());
    }


    @Override
    public boolean deleteNews(long id) {
        return ejb.deleteNews(id);
    }

}
