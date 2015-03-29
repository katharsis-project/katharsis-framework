package io.katharsis.rs.controller;

import io.katharsis.dispatcher.RequestDispatcher;
import io.katharsis.path.JsonPath;
import io.katharsis.response.BaseResponse;
import io.katharsis.rs.controller.annotation.JsonInject;
import io.katharsis.rs.type.JsonApiMediaType;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("{path: .+}")
@Consumes(JsonApiMediaType.APPLICATION_JSON_API)
@Produces(JsonApiMediaType.APPLICATION_JSON_API)
public class JsonApiController {

    @JsonInject
    private JsonPath JsonPath;

    @JsonInject
    private RequestDispatcher requestDispatcher;

    @GET
    public Response getRequest(@PathParam("path") String path) {
        BaseResponse<?> response = requestDispatcher.dispatchRequest(JsonPath, "GET");
        return Response.ok(response).build();
    }
}
