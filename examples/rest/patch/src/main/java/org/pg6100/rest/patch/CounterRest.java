package org.pg6100.rest.patch;

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
@Singleton
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
        return Response.created(URI.create("counters/"+dto.id)).build();
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

    @ApiOperation("Replace a counter")
    @Path("/{id}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void update(@ApiParam("The unique id of the counter")
                       @PathParam("id")
                               Long id,
                       @ApiParam("The new state of the resource")
                       CounterDto dto) {

        if (! dto.id.equals(id)) {
            throw new WebApplicationException(
                    "Cannot modify the counter id from "+id+" to "+dto.id, 409);
        }

        if (dto.value == null) {
            dto.value = 0;
        }

        /*
            Note: here we are allowing PUT to create a new resource
         */
        map.put(id, dto);
    }

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

        map.put(id, dto);
    }


    @ApiOperation("Modify the counter using JSON Merge Patch")
    @Path("/{id}")
    @PATCH
    @Consumes("application/merge-patch+json")
    public void mergePatch(@ApiParam("The unique id of the counter")
                               @PathParam("id")
                                       Long id,
                           @ApiParam("The partial patch")
                                       CounterDto dto){

        //TODO
    }
}
