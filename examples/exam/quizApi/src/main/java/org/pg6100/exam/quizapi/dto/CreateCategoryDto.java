package org.pg6100.exam.quizapi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by arcuri82 on 30-Nov-16.
 */
@ApiModel
public class CreateCategoryDto {

    @ApiModelProperty("The name of the category")
    public String name;

    public CreateCategoryDto() {
    }

    public CreateCategoryDto(String name) {
        this.name = name;
    }

}
