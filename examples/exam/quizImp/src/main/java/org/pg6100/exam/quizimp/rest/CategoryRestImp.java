package org.pg6100.exam.quizimp.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import org.pg6100.exam.quizapi.CategoryRest;
import org.pg6100.exam.quizapi.dto.CreateCategoryDto;
import org.pg6100.exam.quizapi.dto.CreateSubCategoryDto;
import org.pg6100.exam.quizapi.dto.ReadCategoryDto;
import org.pg6100.exam.quizapi.dto.ReadSubCategoryDto;
import org.pg6100.exam.quizimp.data.Category;
import org.pg6100.exam.quizimp.data.SubCategory;
import org.pg6100.exam.quizimp.ejb.CategoryEjb;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.function.Supplier;

import static org.pg6100.exam.quizimp.rest.EjbUtil.ejbCall;

/**
 * Created by arcuri82 on 30-Nov-16.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class CategoryRestImp implements CategoryRest {

    @Context
    private UriInfo uriInfo;

    @EJB
    private CategoryEjb categoryEjb;


    @Override
    public Response createCategory(CreateCategoryDto dto) {

        Long id = ejbCall(() -> categoryEjb.createCategory(dto.name));

        return Response
                .created(uriInfo.getBaseUriBuilder().path("/categories/" + id).build())
                .build();
    }

    @Override
    public List<ReadCategoryDto> getAllCategories(boolean expand) {

        List<Category> categories = ejbCall(() -> categoryEjb.getAllCategories(expand));

        return DtoTransformer.transform(categories, expand);
    }

    @Override
    public ReadCategoryDto getCategory(boolean expand, long id) {

        Category category = ejbCall(() -> categoryEjb.getCategory(id, expand));
        if (category == null) {
            throw new WebApplicationException(404);
        }

        return DtoTransformer.transform(category, expand);
    }

    @Override
    public void deleteCategory(long id) {

        boolean deleted = ejbCall(() -> categoryEjb.deleteCategory(id));
        if (!deleted) {
            throw new WebApplicationException(404);
        }
    }

    @Override
    public void updateCategory(long id, String jsonPatch) {

        verifyCategoryExist(id);

        ObjectMapper jackson = new ObjectMapper();

        JsonNode jsonNode;
        try {
            jsonNode = jackson.readValue(jsonPatch, JsonNode.class);
        } catch (Exception e) {
            throw new WebApplicationException("Invalid JSON data as input: " + e.getMessage(), 400);
        }

        if (jsonNode.has("name")) {
            String newName;
            JsonNode nameNode = jsonNode.get("name");
            if (nameNode.isNull()) {
                newName = null;
            } else if (nameNode.isTextual()) {
                newName = nameNode.asText();
            } else {
                throw new WebApplicationException("Invalid JSON. Non-string name", 400);
            }

            ejbCall(() -> categoryEjb.changeCategoryName(id, newName));
        } else {
            throw new WebApplicationException(400);
        }
    }

    @Override
    public Response getSubCategories(long id) {

        return Response.status(301)
                .location(uriInfo.getBaseUriBuilder()
                        .path("/subcategories")
                        .queryParam("parentId", id)
                        .build()
                ).build();
    }

    @Override
    public Response createSubCategory(long id, CreateSubCategoryDto dto) {

        verifyCategoryExist(id);

        long subId = ejbCall(() -> categoryEjb.createSubCategory(id, dto.name));

        return Response.created(uriInfo.getBaseUriBuilder()
                .path("/subcategories/" + subId).build())
                .build();
    }

    @Override
    public List<ReadSubCategoryDto> getAllSubcategories(Long parentId) {

        List<SubCategory> list = categoryEjb.getSubCategories(parentId);

        return DtoTransformer.transform(list);
    }

    @Override
    public ReadSubCategoryDto getSubCategory(long id) {

        SubCategory subCategory = ejbCall(() -> categoryEjb.getSubCategory(id));
        if(subCategory == null){
            throw new WebApplicationException(404);
        }

        return DtoTransformer.transform(subCategory);
    }


    //----------------------------------------------------------

    private void verifyCategoryExist(long id) {

        boolean exists = ejbCall(() -> categoryEjb.doesCategoryExist(id));
        if (!exists) {
            throw new WebApplicationException(404);
        }
    }


}
