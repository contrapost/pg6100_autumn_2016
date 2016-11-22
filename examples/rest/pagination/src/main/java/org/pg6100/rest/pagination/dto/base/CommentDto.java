package org.pg6100.rest.pagination.dto.base;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "A comment on a news")
public class CommentDto extends BaseDto{

    @ApiModelProperty("The text of the comment")
    public String text;

    public CommentDto() {
    }

    public CommentDto(String text) {
        this.text = text;
    }
}
