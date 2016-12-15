package org.pg6100.exam.gamecommands.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by arcuri82 on 02-Dec-16.
 */
@ApiModel
@XmlRootElement
public class ResultDto extends AnswerDto{

    @ApiModelProperty("Whether the given answer was correct or not")
    public boolean isCorrectAnswer;
}
