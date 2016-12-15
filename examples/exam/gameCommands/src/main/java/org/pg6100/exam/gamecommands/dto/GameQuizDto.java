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
public class GameQuizDto {

    @ApiModelProperty("The id of the quiz played in this game")
    @XmlElement
    public String quizId;

    @ApiModelProperty("The question in this quiz")
    @XmlElement
    public String question;

    @ApiModelProperty("The first possible answer to the question in this quiz")
    @XmlElement
    public String firstAnswer;

    @ApiModelProperty("The second possible answer to the question in this quiz")
    @XmlElement
    public String secondAnswer;

    @ApiModelProperty("The third possible answer to the question in this quiz")
    @XmlElement
    public String thirdAnswer;

    @ApiModelProperty("The fourth possible answer to the question in this quiz")
    @XmlElement
    public String fourthAnswer;
}
