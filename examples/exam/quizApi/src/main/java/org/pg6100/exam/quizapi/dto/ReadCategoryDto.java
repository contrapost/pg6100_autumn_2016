package org.pg6100.exam.quizapi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * Created by arcuri82 on 30-Nov-16.
 */
@ApiModel
public class ReadCategoryDto extends CreateCategoryDto{

    @ApiModelProperty("The uniqe id of the category")
    public String id;

    @ApiModelProperty("The list of subcategories in this category")
    public List<ReadSubCategoryDto> subCategories;

}
