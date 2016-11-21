package org.pg6100.rest.pagination.dto.base;

public class CommentDto extends BaseDto{

    public String text;

    public CommentDto() {
    }

    public CommentDto(String text) {
        this.text = text;
    }
}
