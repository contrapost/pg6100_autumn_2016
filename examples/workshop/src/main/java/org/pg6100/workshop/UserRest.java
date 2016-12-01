package org.pg6100.workshop;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.jaxrs.PATCH;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/users")
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class UserRest {

    @Context
    UriInfo uriInfo;

    @EJB
    private UserEJB ejb;

    @Path("/{id}")
    @GET
    @Produces("application/json")
    public UserDto get(@PathParam("id") Long id){

        UserEntity entity = ejb.getUser(id);
        if(entity == null){
            throw new WebApplicationException(404);
        }

        return DtoTransformer.transform(entity);
    }

    @POST
    @Consumes("application/json")
    public Response create(UserDto dto){
        long id = ejb.createUser(dto.name, dto.surname, dto.address);

        return Response.created( uriInfo.getBaseUriBuilder()
                .path("/users/" + id)
                .build()).build();
    }

    @Path("/{id}")
    @PUT
    @Consumes("application/json")
    public void replace(@PathParam("id") Long id, UserDto dto){
        if(ejb.getUser(id)== null){
            throw new WebApplicationException(404);
        }
        ejb.update(id, dto.name, dto.surname, dto.address);
    }

    @Path("/{id}")
    @PATCH
    @Consumes("application/merge-patch+json")
    public void update(@PathParam("id") Long id, String jsonPatch){

        UserEntity entity = ejb.getUser(id);

        if(entity == null){
            throw new WebApplicationException(404);
        }

        ObjectMapper jackson = new ObjectMapper();

        JsonNode jsonNode;
        try {
            jsonNode = jackson.readValue(jsonPatch, JsonNode.class);
        } catch (Exception e) {
            throw new WebApplicationException("Invalid JSON data as input: " + e.getMessage(), 400);
        }

        String name = entity.getName();
        String surname = entity.getSurname();
        String address = entity.getAddress();

        if (jsonNode.has("name")) {
            JsonNode node = jsonNode.get("name");
            if (node.isNull()) {
                name = null;
            } else if (node.isTextual()) {
                name = node.asText();
            } else {
                throw new WebApplicationException("Invalid JSON. Non-string name", 400);
            }
        }
        if (jsonNode.has("surname")) {
            JsonNode node = jsonNode.get("surname");
            if (node.isNull()) {
                surname = null;
            } else if (node.isTextual()) {
                surname = node.asText();
            } else {
                throw new WebApplicationException("Invalid JSON. Non-string surname", 400);
            }
        }
        if (jsonNode.has("address")) {
            JsonNode node = jsonNode.get("address");
            if (node.isNull()) {
                address = null;
            } else if (node.isTextual()) {
                address = node.asText();
            } else {
                throw new WebApplicationException("Invalid JSON. Non-string address", 400);
            }
        }

        ejb.update(id, name, surname, address);
    }
}
