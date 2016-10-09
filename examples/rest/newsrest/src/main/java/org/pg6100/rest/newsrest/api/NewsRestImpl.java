package org.pg6100.rest.newsrest.api;

import org.pg6100.news.NewsEJB;
import org.pg6100.news.country.CountryList;
import org.pg6100.rest.newsrest.dto.NewsConverter;
import org.pg6100.rest.newsrest.dto.NewsDto;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;

@Stateless
public class NewsRestImpl implements NewsRestApi{

    @EJB
    private NewsEJB ejb;


    @Override
    public List<NewsDto> get() {
        return NewsConverter.transform(ejb.getAll());
    }

    @Override
    public List<NewsDto> getByCountry(String country) {

//        if(!CountryList.isValidCountry(country)){
//            throw new
//        }


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
        return ejb.createNews(dto.authorId, dto.text, dto.country);
    }

    @Override
    public NewsDto getById(Long id) {
        return NewsConverter.transform(ejb.get(id));
    }

    @Override
    public void update(NewsDto dto) {
        ejb.updateText(Long.parseLong(dto.id), dto.text);
    }

    @Override
    public void delete(Long id) {
        ejb.deleteNews(id);
    }
}
