package io.katharsis.core.internal.dispatcher.controller;

import io.katharsis.core.internal.dispatcher.path.JsonPath;
import io.katharsis.errorhandling.exception.RequestBodyException;
import io.katharsis.legacy.internal.RepositoryMethodParameterProvider;
import io.katharsis.repository.request.HttpMethod;
import io.katharsis.repository.request.QueryAdapter;
import io.katharsis.repository.response.JsonApiResponse;
import io.katharsis.repository.response.Response;
import io.katharsis.resource.Document;
import io.katharsis.resource.registry.RegistryEntry;

/**
 * Represents a controller contract. There can be many kinds of requests that can be send to the framework. The
 * initial process of checking if a request is acceptable is managed by
 * {@link BaseController#isAcceptable(io.katharsis.core.internal.dispatcher.path.JsonPath, String)} method. If the method returns
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
     * @param queryAdapter      QueryAdapter
     * @param parameterProvider repository method parameter provider
     * @param requestBody       Top-level JSON object from method's body of the request passed as {@link RequestBody}
     * @return BaseResponseContext object
     */
    public abstract Response handle(JsonPath jsonPath, QueryAdapter queryAdapter, RepositoryMethodParameterProvider
            parameterProvider, Document document);


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
