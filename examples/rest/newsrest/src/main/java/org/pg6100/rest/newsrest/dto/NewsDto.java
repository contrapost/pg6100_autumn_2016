package org.pg6100.rest.newsrest.dto;

import java.time.LocalDateTime;

public class NewsDto {

    public String id;
    public String authorId;
    public String text;
    public String country;
    public LocalDateTime creationTime;

    public NewsDto(){}

    public NewsDto(String id, String authorId, String text, String country, LocalDateTime creationTime) {
        this.id = id;
        this.authorId = authorId;
        this.text = text;
        this.country = country;
        this.creationTime = creationTime;
    }
}
