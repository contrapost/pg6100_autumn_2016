package org.pg6100.rest.pagination.dto.base;

import java.util.List;

public class NewsDto  extends BaseDto {

    public String text;

    public String country;

    public List<CommentDto> comments;

    public List<VoteDto> votes;


    public NewsDto(){}

    public NewsDto(String text, String country, List<CommentDto> comments, List<VoteDto> votes) {
        this.text = text;
        this.country = country;
        this.comments = comments;
        this.votes = votes;
    }
}
