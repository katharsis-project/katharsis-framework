package io.katharsis.rs;

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

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static io.katharsis.rs.type.JsonApiMediaType.APPLICATION_JSON_API_TYPE;

/**
 * Handles JSON API requests.
 * <p>
 * Consumes: <i>null</i> | {@link JsonApiMediaType}
 * Produces: {@link JsonApiMediaType}
 * </p>
 * <p>
 * To be able to send a request to Katharsis it is necessary to provide full media type alongside the request.
 * Wildcards are not accepted.
 * </p>
 */
@PreMatching
public class KatharsisFilter implements ContainerRequestFilter {

    private ObjectMapper objectMapper;
    private ResourceRegistry resourceRegistry;
    private RequestDispatcher requestDispatcher;

    public KatharsisFilter(ObjectMapper objectMapper, ResourceRegistry resourceRegistry, RequestDispatcher
            requestDispatcher) {
        this.objectMapper = objectMapper;
        this.resourceRegistry = resourceRegistry;
        this.requestDispatcher = requestDispatcher;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (isAcceptableMediaType(requestContext) && isAcceptableContentType(requestContext)) {
            dispatchRequest(requestContext);
        }
    }

    private void dispatchRequest(ContainerRequestContext requestContext) {
        UriInfo uriInfo = requestContext.getUriInfo();
        JsonPath jsonPath = new PathBuilder(resourceRegistry).buildPath(uriInfo.getPath());
        RequestParams requestParams = createRequestParams(uriInfo);

        BaseResponse<?> responseData = requestDispatcher.dispatchRequest(jsonPath, requestContext.getMethod(), requestParams);
        Response response;
        if (responseData != null) {
            response = Response.ok(responseData, APPLICATION_JSON_API_TYPE).build();
        } else {
            response = Response.noContent().build();
        }
        requestContext.abortWith(response);
    }

    private boolean isAcceptableMediaType(ContainerRequestContext requestContext) {
        boolean result = false;
        for (MediaType acceptableType : requestContext.getAcceptableMediaTypes()) {
            if (APPLICATION_JSON_API_TYPE.getType().equalsIgnoreCase(acceptableType.getType()) &&
                    APPLICATION_JSON_API_TYPE.getSubtype().equalsIgnoreCase(acceptableType.getSubtype())) {
                result = true;
            }
        }
        return result;
    }

    private boolean isAcceptableContentType(ContainerRequestContext requestContext) {
        MediaType contentType = requestContext.getMediaType();
        return contentType == null || APPLICATION_JSON_API_TYPE.isCompatible(contentType);
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
