package org.pg6100.exam.quizapi;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.PATCH;
import org.pg6100.exam.quizapi.dto.hal.ListDto;
import org.pg6100.exam.quizapi.dto.CreateQuizDto;
import org.pg6100.exam.quizapi.dto.ReadQuizDto;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * Created by arcuri82 on 30-Nov-16.
 */
@Api(value = "Quizzes")
@Path("/quizzes")
public interface QuizRest {

    @ApiOperation("Retrieve a page of quiz definitions")
    @GET
    @Produces(Formats.JSON_V1)
    ListDto<ReadQuizDto> getQuizzes(@ApiParam("Offset in the list of quizzes")
                                    @QueryParam("offset")
                                    @DefaultValue("0")
                                            Integer offset,
                                    @ApiParam("Limit of quizzes in a single retrieved page")
                                    @QueryParam("limit")
                                    @DefaultValue("10")
                                            Integer limit,
                                    @ApiParam("An optional filter specifying the id of a subcategory. " +
                                            "If present, retrieve only the quizzes for such subcategory")
                                    @QueryParam("filter")
                                        String filter
                                    );

    @ApiOperation("Create a new quiz")
    @POST
    @Consumes(Formats.JSON_V1)
    Response createQuiz(CreateQuizDto dto);


    @ApiOperation("Get the quiz specified by id")
    @Path("/{id}")
    @GET
    @Produces(Formats.JSON_V1)
    ReadQuizDto getQuiz( @PathParam("id")
                                 long id);

    @ApiOperation("Delete the given quiz specified by id")
    @Path("/{id}")
    @DELETE
    void deleteQuiz( @PathParam("id")
                                 long id);


    @ApiOperation("Do a JSON Merge Patch on the given quiz specified by id")
    @Path("/{id}")
    @PATCH
    @Consumes(Formats.MERGE_PATCH_V1)
    void updateQuiz( @PathParam("id")
                             long id,
                     String mergePatch);

    @ApiOperation("Return one quiz at random")
    @Path("/random")
    @GET
    @Produces(Formats.JSON_V1)
    Response getQuizAtRandom();

}
