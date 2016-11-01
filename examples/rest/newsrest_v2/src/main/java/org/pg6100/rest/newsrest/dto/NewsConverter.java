package org.pg6100.rest.newsrest.dto;

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
        //here setup both ids, even if one is deprecated
        dto.id = String.valueOf(entity.getId());
        dto.newsId = dto.id;

        dto.authorId = entity.getAuthorId();
        dto.text = entity.getText();
        dto.country = entity.getCountry();
        dto.creationTime = entity.getCreationTime();

        return dto;
    }

    public static List<NewsDto> transform(List<NewsEntity> entities){
        Objects.requireNonNull(entities);

        return entities.stream()
                .map(NewsConverter::transform)
                .collect(Collectors.toList());
    }

}
