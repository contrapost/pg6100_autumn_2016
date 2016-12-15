package org.pg6100.exam.quizapi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by arcuri82 on 30-Nov-16.
 */
@ApiModel
public class ReadQuizDto extends CreateQuizDto {

    @ApiModelProperty("The unique id of the quiz")
    public String id;
}
