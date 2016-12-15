package org.pg6100.exam.quizimp.rest;

import org.pg6100.exam.quizapi.dto.hal.ListDto;
import org.pg6100.exam.quizapi.dto.ReadCategoryDto;
import org.pg6100.exam.quizapi.dto.ReadQuizDto;
import org.pg6100.exam.quizapi.dto.ReadSubCategoryDto;
import org.pg6100.exam.quizimp.data.Category;
import org.pg6100.exam.quizimp.data.Quiz;
import org.pg6100.exam.quizimp.data.SubCategory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by arcuri82 on 30-Nov-16.
 */
public class DtoTransformer {

    public static ReadQuizDto transform(Quiz quiz){

        ReadQuizDto dto = new ReadQuizDto();
        dto.id = String.valueOf(quiz.getId());
        dto.subCategoryId = String.valueOf(quiz.getSubCategoryId());
        dto.question = quiz.getQuestion();
        dto.firstAnswer = quiz.getFirstAnswer();
        dto.secondAnswer = quiz.getSecondAnswer();
        dto.thirdAnswer = quiz.getThirdAnswer();
        dto.fourthAnswer = quiz.getFourthAnswer();
        dto.indexOfCorrectAnswer = quiz.getIndexOfCorrectAnswer();

        return dto;
    }

    public static ReadCategoryDto transform(Category entity, boolean expand){

        ReadCategoryDto dto = new ReadCategoryDto();
        dto.id = String.valueOf(entity.getId());
        dto.name = entity.getName();

        if(expand){
            dto.subCategories = transform(entity.getSubCategories());
        }

        return dto;
    }

    public static ReadSubCategoryDto transform(SubCategory entity){

        ReadSubCategoryDto dto = new ReadSubCategoryDto();
        dto.id = String.valueOf(entity.getId());
        dto.name = entity.getName();
        dto.parentCategoryId = String.valueOf(entity.getParent().getId());

        return dto;
    }

    public static List<ReadSubCategoryDto> transform(List<SubCategory> entities){

        return entities.stream()
                .map(DtoTransformer::transform)
                .collect(Collectors.toList());
    }

    public static List<ReadCategoryDto> transform(List<Category> entities, boolean expand){

        return entities.stream()
                .map(c -> transform(c, expand))
                .collect(Collectors.toList());
    }


    public static ListDto<ReadQuizDto> transform(List<Quiz> quizList,
                                             int offset,
                                             int limit) {
        List<ReadQuizDto> dtoList = null;
        if (quizList != null) {
            dtoList = quizList.stream()
                    .skip(offset)
                    .limit(limit)
                    .map(DtoTransformer::transform)
                    .collect(Collectors.toList());
        }

        ListDto<ReadQuizDto> dto = new ListDto<>();
        dto.list = dtoList;
        dto._links = new ListDto.ListLinks();
        dto.rangeMin = offset;
        dto.rangeMax = dto.rangeMin + dtoList.size() - 1;
        dto.totalSize = quizList.size();

        return dto;
    }
}
