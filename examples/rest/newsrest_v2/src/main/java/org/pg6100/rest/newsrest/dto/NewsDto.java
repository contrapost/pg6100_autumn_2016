package org.pg6100.rest.newsrest.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.pg6100.news.constraint.Country;

import java.time.ZonedDateTime;

/*
    A data transfer object (DTO) is what we will use to represent and (un)marshal
     JSon objects.
     Note: it is perfectly fine that fields here are all "public".
     This is just a POJO (plain old Java object), with no logic, just data.
     Also note how Swagger is used here to provide documentation.
 */


@ApiModel("A news")
public class NewsDto {

    @ApiModelProperty("Deprecated. Use newsId instead")
    @Deprecated
    public String id;

    /*
        This is the easiest way to handle changing the name of a
        property, eg from "id" to "newsId":
        just add a new property, and mark "Deprecated" the old.
        Then sending data, just send both.
        When looking at which id to use, first look at "newsId" and,
        if missing, only then look at "id".

        Note: to make things more sophisticated, we could avoid
        sending "id" when a client ask for the "new" format.
        But to do that, we need to modify how Jackson do marshalling.

        If there are many changes, another option is to create a new DTO
        for the new version
     */

    @ApiModelProperty("The id of the news")
    public String newsId;


    @ApiModelProperty("The id of the author that wrote/created this news")
    public String authorId;

    @ApiModelProperty("The text of the news")
    public String text;

    @ApiModelProperty("The country this news is related to")
    @Country
    public String country;

    @ApiModelProperty("When the news was first created/published")
    public ZonedDateTime creationTime;

    public NewsDto(){}

    public NewsDto(String id, String authorId, String text, String country, ZonedDateTime creationTime) {
        this.id = id;
        this.newsId = id;  //note: setting both of them

        this.authorId = authorId;
        this.text = text;
        this.country = country;
        this.creationTime = creationTime;
    }
}
