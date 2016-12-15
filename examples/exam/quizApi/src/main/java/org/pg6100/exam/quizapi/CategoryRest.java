package org.pg6100.exam.quizapi;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.PATCH;
import org.pg6100.exam.quizapi.dto.CreateCategoryDto;
import org.pg6100.exam.quizapi.dto.CreateSubCategoryDto;
import org.pg6100.exam.quizapi.dto.ReadCategoryDto;
import org.pg6100.exam.quizapi.dto.ReadSubCategoryDto;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by arcuri82 on 30-Nov-16.
 */
@Api(value = "Categories")
@Path("/")
public interface CategoryRest {

    @ApiOperation("Create a new category")
    @Path("/categories")
    @POST
    @Consumes(Formats.JSON_V1)
    Response createCategory(CreateCategoryDto dto);


    @ApiOperation("Get all current existing categories")
    @Path("/categories")
    @GET
    @Produces(Formats.JSON_V1)
    List<ReadCategoryDto> getAllCategories(
            @ApiParam("Whether the subcategories should be returned as well")
            @QueryParam("expand")
            @DefaultValue("false")
                    boolean expand);


    @ApiOperation("Get the category identified by the given unique id")
    @Path("/categories/{id}")
    @GET
    @Produces(Formats.JSON_V1)
    ReadCategoryDto getCategory(
            @ApiParam("Whether the subcategories should be returned as well")
            @QueryParam("expand")
            @DefaultValue("false")
                    boolean expand,
            @PathParam("id")
                    long id);

    @ApiOperation("Delete the given category specified by id")
    @Path("/categories/{id}")
    @DELETE
    void deleteCategory(@PathParam("id")
                                long id);

    @ApiOperation("Do a JSON Merge Patch on the given category specified by id")
    @Path("/categories/{id}")
    @PATCH
    @Consumes(Formats.MERGE_PATCH_V1)
    void updateCategory(@PathParam("id")
                                long id,
                        String jsonPatch);


    @ApiOperation("Get all the subcategories in the given category specified by id")
    @Path("/categories/{id}/subcategories")
    @GET
    Response getSubCategories(
            @PathParam("id")
                    long id);

    @ApiOperation("Create a new subcategory in the given category specified by id")
    @Path("/categories/{id}/subcategories")
    @POST
    @Consumes(Formats.JSON_V1)
    Response createSubCategory(
            @PathParam("id")
                    long id,
            CreateSubCategoryDto dto);


    @ApiOperation("Get all existing subcategories")
    @Path("/subcategories")
    @GET
    List<ReadSubCategoryDto> getAllSubcategories(
            @ApiParam("Restrict the retrieved subcategories to only the ones under the given category")
            @QueryParam("parentId")
                    Long parentId
    );

    @ApiOperation("Get the subcategory specified by id")
    @Path("/subcategories/{id}")
    @GET
    ReadSubCategoryDto getSubCategory(@PathParam("id")
                                              long id);

}
