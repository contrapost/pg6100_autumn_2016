package org.pg6100.exam.gamecommands.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by arcuri82 on 02-Dec-16.
 */
@ApiModel
@XmlRootElement
public class AnswerDto {

    @ApiModelProperty("The unique id of a quiz")
    @XmlElement
    public String quizId;

    @ApiModelProperty("Index (0 to n-1) of the chosen answer for the quiz")
    @XmlElement
    public Integer chosenAnswerIndex;

    public AnswerDto() {
    }

    public AnswerDto(String quizId, Integer chosenAnswerIndex) {
        this.quizId = quizId;
        this.chosenAnswerIndex = chosenAnswerIndex;
    }
}
