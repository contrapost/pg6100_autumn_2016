package org.pg6100.rest.patch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.PATCH;

import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Api("Handle named, numeric counters")
@Path("/counters")
@Singleton  // recall, this does synchronize all the methods...
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class CounterRest {

    private final AtomicLong idGenerator = new AtomicLong(0);

    /**
     * Map for the dtos, where the key is the dto.id
     */
    private final Map<Long, CounterDto> map = new ConcurrentHashMap();

    @ApiOperation("Create a new counter")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(CounterDto dto) {

        if (dto.id == null) {
            dto.id = idGenerator.getAndIncrement();
        } else if (map.containsKey(dto.id)) {
            throw new WebApplicationException("Cannot create a new counter with id " + dto.id + "that already exist", 400);
        }

        if (dto.value == null) {
            dto.value = 0;
        }

        map.put(dto.id, dto);

        /*
            here created will set the "Location" header, by specifying the URI of where we can GET it from.
            A relative (ie not absolute) URI will resolve against the URI of the request, ie "/patch/api/counters".
            Note that the response here has no body.
         */
        return Response.created(URI.create("counters/" + dto.id)).build();
    }

    @ApiOperation("Get all the existing counters")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<CounterDto> getAll() {
        return map.values().stream().collect(Collectors.toList());
    }


    @ApiOperation("Return the counter with the given id")
    @Path("/{id}")
    @GET
    public CounterDto get(
            @ApiParam("The unique id of the counter")
            @PathParam("id")
                    Long id) {

        CounterDto dto = map.get(id);
        if (dto == null) {
            throw new WebApplicationException("No counter with id " + id + " exists", 404);
        }

        return dto;
    }


    /*
        A PUT does completely replace the resource with a new one
     */

    @ApiOperation("Replace a counter")
    @Path("/{id}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void update(@ApiParam("The unique id of the counter")
                       @PathParam("id")
                               Long id,
                       @ApiParam("The new state of the resource")
                               CounterDto dto) {

        if (!dto.id.equals(id)) {
            throw new WebApplicationException(
                    "Cannot modify the counter id from " + id + " to " + dto.id, 409);
        }

        if (dto.value == null) {
            dto.value = 0;
        }

        /*
            Note: here we are allowing PUT to create a new resource
         */
        map.put(id, dto);
    }


    /*
        A PATCH does a partial update of a resource.
        However, there is no strict rule on how the resource should be
        updated.
        The client has to send "instructions" in a format that the server
        can recognize and execute.
        Can also be a custom format, like in this case.
     */

    @ApiOperation("Modify the counter based on the instructions in the request body")
    @Path("/{id}")
    @PATCH
    @Consumes(MediaType.TEXT_PLAIN) // could have had a custom type here, but then would need unmarshaller for it
    public void patch(@ApiParam("The unique id of the counter")
                      @PathParam("id")
                              Long id,
                      //
                      @ApiParam("The instructions on how to modify the counter. " +
                              "In this specific case, it should be a single numeric value " +
                              "representing a delta that " +
                              "will be applied on the counter value")
                              String text) {

        CounterDto dto = map.get(id);
        if (dto == null) {
            throw new WebApplicationException("Cannot find counter with id " + id, 404);
        }

        int delta;

        try {
            delta = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new WebApplicationException("Invalid instructions. Should contain just a number: \"" + text + "\"");
        }

        dto.value += delta;
    }


    /*
        When dealing with resources that can be expressed/modelled with JSon,
        there are two main formats for instructions:
        - (1) JSON Patch
        - (2) JSON Merge Patch

        (1) provide a list of operations (eg, add, remove, move, test).
        It is more expressive than (2), but
        at the same time more complicated to handle.

        (2) is just sending a subset of the JSon, and each of specified
        changes will be applied, ie overwritten.
        The only tricky thing to keep in mind is the handling of "null"
        values. Missing/unspecified elements will be ignored, whereas
        elements with null will get null.
        For example, if you have:
        {"A":... , "B":...} as JSon resource, and you get a patch with:
        {"B":null}, then A will not be modified, whereas B will be removed,
        ie results of the resource would be:
        {"A":...}
        without the B.
        Note: in JSon there is a difference between a missing element and
        and an element with null, ie
        {"A":...} and
        {"A":... , "B":null}
        are technically not the same.

        However, as we will deal with Java objects, we can safely "ignore"
        this distinction in our model: ie make no difference between a
        missing element and an element with null value.

        The only catch is in the parsing of the PATCH JSon merge objects.
        We CANNOT unmarshal it to a DTO, as we would have no way to distinguish
        between missing elements and actual null values.
     */

    @ApiOperation("Modify the counter using JSON Merge Patch")
    @Path("/{id}")
    @PATCH
    @Consumes("application/merge-patch+json")
    public void mergePatch(@ApiParam("The unique id of the counter")
                           @PathParam("id")
                                   Long id,
                           @ApiParam("The partial patch")
                                   String jsonPatch) {

        CounterDto dto = map.get(id);
        if (dto == null) {
            throw new WebApplicationException("Cannot find counter with id " + id, 404);
        }

        ObjectMapper jackson = new ObjectMapper();

        JsonNode jsonNode;
        try {
            jsonNode = jackson.readValue(jsonPatch, JsonNode.class);
        } catch (Exception e) {
            throw new WebApplicationException("Invalid JSON data as input: " + e.getMessage(), 400);
        }

        if (jsonNode.has("id")) {
            throw new WebApplicationException(
                    "Cannot modify the counter id from " + id + " to " + jsonNode.get("id"), 409);
        }

        //do not alter dto till all data is validated. A PATCH has to be atomic,
        //either all modifications are done, or none.
        String newName = dto.name;
        int newValue = dto.value;

        if (jsonNode.has("name")) {
            JsonNode nameNode = jsonNode.get("name");
            if (nameNode.isNull()) {
                newName = null;
            } else if (nameNode.isTextual()) {
                newName = nameNode.asText();
            } else {
                throw new WebApplicationException("Invalid JSON. Non-string name", 400);
            }

            //Doing this here would be wrong!!!
            //eg, what happened if "value" parsing fail? we ll have side-effects
            //
            //dto.name = newName;
        }

        if (jsonNode.has("value")) {

            JsonNode valueNode = jsonNode.get("value");

            if (valueNode.isNull()) {
                newValue = 0;
            } else if (valueNode.isNumber()) {
                //note: if this is not numeric, it silently returns 0...
                newValue = valueNode.intValue();
            } else {
                throw new WebApplicationException(
                        "Invalid JSON. Non-numeric value: " + valueNode.asText(), 400);
            }
        }

        //now that the input is validated, do the update
        dto.name = newName;
        dto.value = newValue;
    }
}
