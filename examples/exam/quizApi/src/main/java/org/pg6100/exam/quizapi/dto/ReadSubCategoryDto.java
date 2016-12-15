package org.pg6100.exam.quizapi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by arcuri82 on 30-Nov-16.
 */
@ApiModel
public class ReadSubCategoryDto  extends CreateSubCategoryDto{

    @ApiModelProperty("The unique id of this subcategory")
    public String id;

    @ApiModelProperty("The id of the category this subcategory belongs to")
    public String parentCategoryId;

}
