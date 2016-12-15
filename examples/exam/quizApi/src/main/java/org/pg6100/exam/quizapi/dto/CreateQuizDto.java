package org.pg6100.exam.quizapi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by arcuri82 on 30-Nov-16.
 */
@ApiModel
public class CreateQuizDto {

    @ApiModelProperty("The id of the subcategory this quiz belongs to")
    public String subCategoryId;

    @ApiModelProperty("The question in this quiz")
    public String question;

    @ApiModelProperty("The first possible answer to the question in this quiz")
    public String firstAnswer;

    @ApiModelProperty("The second possible answer to the question in this quiz")
    public String secondAnswer;

    @ApiModelProperty("The third possible answer to the question in this quiz")
    public String thirdAnswer;

    @ApiModelProperty("The fourth possible answer to the question in this quiz")
    public String fourthAnswer;

    @ApiModelProperty("The index (from 0 to n-1) of the only answer that is correct in this quiz")
    public Integer indexOfCorrectAnswer;


    public CreateQuizDto(){}

    public CreateQuizDto(String subCategoryId, String question, String firstAnswer, String secondAnswer, String thirdAnswer, String fourthAnswer, Integer indexOfCorrectAnswer) {
        this.subCategoryId = subCategoryId;
        this.question = question;
        this.firstAnswer = firstAnswer;
        this.secondAnswer = secondAnswer;
        this.thirdAnswer = thirdAnswer;
        this.fourthAnswer = fourthAnswer;
        this.indexOfCorrectAnswer = indexOfCorrectAnswer;
    }
}
