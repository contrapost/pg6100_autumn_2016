package org.pg6100.rest.newsrest.dto;

import org.pg6100.news.NewsEntity;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class NewsConverter {

    private NewsConverter(){}

    public static NewsDto transform(NewsEntity entity){
        Objects.requireNonNull(entity);

        NewsDto dto = new NewsDto();
        dto.id = String.valueOf(entity.getId());
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
