package io.katharsis.dispatcher.controller;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.resource.exception.RequestBodyException;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.JsonApiResponse;

/**
 * Represents a controller contract. There can be many kinds of requests that can be send to the framework. The
 * initial process of checking if a request is acceptable is managed by
 * {@link BaseController#isAcceptable(io.katharsis.request.path.JsonPath, String)} method. If the method returns
 * true, the matched controller is used to handle the request.
 */
public abstract class BaseController {

    /**
     * Checks if requested resource method is acceptable.
     *
     * @param jsonPath    Requested resource path
     * @param requestType HTTP request type
     * @return Acceptance result in boolean
     */
    public abstract boolean isAcceptable(JsonPath jsonPath, String requestType);

    /**
     * Passes the request to controller method.
     *
     * @param jsonPath          Requested resource path
     * @param parameterProvider repository method parameter provider
     * @param queryParams       Params specifying request
     * @param requestBody       Top-level JSON object from method's body of the request passed as {@link RequestBody}
     * @return BaseResponseContext object
     */
    public abstract BaseResponseContext handle(JsonPath jsonPath, QueryParams queryParams, RepositoryMethodParameterProvider
        parameterProvider, RequestBody requestBody);


    protected void verifyTypes(HttpMethod methodType, String resourceEndpointName, RegistryEntry endpointRegistryEntry,
                             RegistryEntry bodyRegistryEntry) {
        if (endpointRegistryEntry.equals(bodyRegistryEntry)) {
            return;
        }
        if (bodyRegistryEntry == null || !bodyRegistryEntry.isParent(endpointRegistryEntry)) {
            String message = String.format("Inconsistent type definition between path and body: body type: " +
                "%s, request type: %s", methodType, resourceEndpointName);
            throw new RequestBodyException(methodType, resourceEndpointName, message);
        }
    }

    protected Object extractResource(Object responseOrResource) {
        if (responseOrResource instanceof JsonApiResponse) {
            return ((JsonApiResponse) responseOrResource).getEntity();
        } else {
            return responseOrResource;
        }
    }
}
