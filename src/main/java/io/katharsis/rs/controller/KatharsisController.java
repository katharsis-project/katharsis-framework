package io.katharsis.rs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.RequestDispatcher;
import io.katharsis.jackson.exception.JsonDeserializationException;
import io.katharsis.path.JsonPath;
import io.katharsis.path.PathBuilder;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
import io.katharsis.rs.type.JsonApiMediaType;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

@Path("{path: [^?]+}")
@Consumes(JsonApiMediaType.APPLICATION_JSON_API)
@Produces(JsonApiMediaType.APPLICATION_JSON_API)
public class KatharsisController {

    private ObjectMapper objectMapper;
    private ResourceRegistry resourceRegistry;
    private RequestDispatcher requestDispatcher;

    public KatharsisController(ObjectMapper objectMapper, ResourceRegistry resourceRegistry, RequestDispatcher
            requestDispatcher) {
        this.objectMapper = objectMapper;
        this.resourceRegistry = resourceRegistry;
        this.requestDispatcher = requestDispatcher;
    }

    @GET
    public Response getRequest(@Context HttpServletRequest request, @Context UriInfo uriInfo) {
        JsonPath jsonPath = new PathBuilder(resourceRegistry).buildPath(request.getRequestURI());
        RequestParams requestParams = createRequestParams(uriInfo);

        BaseResponse<?> responseData = requestDispatcher.dispatchRequest(jsonPath, "GET", requestParams);
        Response response;
        if (responseData != null) {
            response = Response.ok(responseData).build();
        } else {
            response = Response.noContent().build();
        }
        return response;
    }

    private RequestParams createRequestParams(UriInfo uriInfo) {
        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(objectMapper);

        MultivaluedMap<String, String> queryParametersMultiMap = uriInfo.getQueryParameters();
        Map<String, String> queryParameters = new HashMap<>();

        for (String queryName : queryParametersMultiMap.keySet()) {
            queryParameters.put(queryName, queryParametersMultiMap.getFirst(queryName));
        }

        RequestParams requestParams;
        try {
            requestParams = queryParamsBuilder.buildRequestParams(queryParameters);
        } catch (JsonDeserializationException e) {
            throw new RuntimeException(e);
        }
        return requestParams;
    }
}
