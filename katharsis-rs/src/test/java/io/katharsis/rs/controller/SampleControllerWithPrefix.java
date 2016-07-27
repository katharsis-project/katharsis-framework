package io.katharsis.rs.controller;

import io.katharsis.rs.type.JsonApiMediaType;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Consumes(JsonApiMediaType.APPLICATION_JSON_API)
@Produces(JsonApiMediaType.APPLICATION_JSON_API)
@Path("/api/v1/tasks")
public class SampleControllerWithPrefix {

    public static final String NON_KATHARSIS_RESOURCE_RESPONSE = "NON_KATHARSIS_RESOURCE_RESPONSE";

    @GET
    @Path("sample")
    public Response getRequest() {
        return Response.ok(NON_KATHARSIS_RESOURCE_RESPONSE).build();
    }
}
