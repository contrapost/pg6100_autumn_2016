package org.pg6100.exam.quizapi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by arcuri82 on 30-Nov-16.
 */
@ApiModel
public class CreateSubCategoryDto {

    @ApiModelProperty("The name of the subcategory")
    public String name;

    public CreateSubCategoryDto() {
    }

    public CreateSubCategoryDto(String name) {
        this.name = name;
    }
}
