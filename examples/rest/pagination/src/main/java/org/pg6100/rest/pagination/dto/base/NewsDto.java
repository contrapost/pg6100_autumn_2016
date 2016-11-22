package org.pg6100.rest.pagination.dto.base;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;


@ApiModel(description = "A news")
public class NewsDto  extends BaseDto {

    @ApiModelProperty("The text of the news")
    public String text;

    @ApiModelProperty("The country this news is related to")
    public String country;

    @ApiModelProperty("A list of comments made by users on this news")
    public List<CommentDto> comments;

    @ApiModelProperty("A list of votes of users that liked this news")
    public List<VoteDto> votes;


    public NewsDto(){}

    public NewsDto(String text, String country, List<CommentDto> comments, List<VoteDto> votes) {
        this.text = text;
        this.country = country;
        this.comments = comments;
        this.votes = votes;
    }
}
