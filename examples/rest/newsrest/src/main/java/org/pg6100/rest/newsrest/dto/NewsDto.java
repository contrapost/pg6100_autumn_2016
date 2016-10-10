package org.pg6100.rest.newsrest.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.pg6100.news.constraint.Country;

import java.time.LocalDateTime;

@ApiModel("A news")
public class NewsDto {

    @ApiModelProperty("The id of the news")
    public String id;

    @ApiModelProperty("The id of the author that wrote/created this news")
    public String authorId;

    @ApiModelProperty("The text of the news")
    public String text;

    @ApiModelProperty("The country this news is related to")
    @Country
    public String country;

    @ApiModelProperty("When the news was first created/published")
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
