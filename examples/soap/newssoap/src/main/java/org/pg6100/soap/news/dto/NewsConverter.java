package org.pg6100.soap.news.dto;

import org.pg6100.news.NewsEntity;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/*
    It might be tempting to use the @Entity objects directly as DTOs.
    That would be WRONG!!!
    It might work for small applications developed by one single person,
    but it is a huge problem (eg, maintainability and de-coupling) for
    larger projects.

    So here we need a converter from @Entity to DTO
 */
public class NewsConverter {

    private NewsConverter(){}

    public static NewsDto transform(NewsEntity entity){
        Objects.requireNonNull(entity);

        NewsDto dto = new NewsDto();
        dto.newsId = entity.getId();

        dto.authorId = entity.getAuthorId();
        dto.text = entity.getText();
        dto.country = entity.getCountry();

        return dto;
    }

    public static List<NewsDto> transform(List<NewsEntity> entities){
        Objects.requireNonNull(entities);

        return entities.stream()
                .map(NewsConverter::transform)
                .collect(Collectors.toList());
    }

}
