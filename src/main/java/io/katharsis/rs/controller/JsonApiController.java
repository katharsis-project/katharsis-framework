package io.katharsis.rs.controller;

import io.katharsis.dispatcher.RequestDispatcher;
import io.katharsis.dispatcher.controller.CollectionGet;
import io.katharsis.dispatcher.controller.ResourceGet;
import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.path.ResourcePath;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.rs.controller.annotation.JsonInject;
import io.katharsis.rs.type.JsonApiMediaType;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("{path: .+}")
@Consumes(JsonApiMediaType.APPLICATION_JSON_API)
@Produces(JsonApiMediaType.APPLICATION_JSON_API)
public class JsonApiController {

    @JsonInject
    private ResourcePath resourcePath;

    @JsonInject
    private ResourceRegistry resourceRegistry;

    @GET
    public Response getRequest(@PathParam("path") String path) {
        RequestDispatcher dispatcher = new RequestDispatcher(new ControllerRegistry(new CollectionGet(resourceRegistry), new ResourceGet(resourceRegistry)));
        return Response.ok(dispatcher.dispatchRequest(resourcePath, "GET")).build();
    }
}
