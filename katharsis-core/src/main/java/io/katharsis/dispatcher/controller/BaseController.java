package io.katharsis.dispatcher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.jackson.exception.JsonSerializationException;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.Request;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.resource.exception.RequestBodyException;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.utils.parser.TypeParser;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Represents a controller contract. There can be many kinds of requests that can be send to the framework. The
 * initial process of checking if a request is acceptable is managed by
 * {@link BaseController#isAcceptable(io.katharsis.request.Request)} method. If the method returns
 * true, the matched controller is used to handle the request.
 */
public abstract class BaseController {

    @Getter
    protected final ObjectMapper objectMapper;

    public BaseController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Checks if requested resource method is acceptable.
     *
     * @param request the HTTP Request from the client
     * @return Acceptance result in boolean
     */
    public abstract boolean isAcceptable(Request request);

    public abstract TypeParser getTypeParser();

    public abstract QueryParamsBuilder getQueryParamsBuilder();

    /**
     * Passes the request to controller method.
     *
     * @param request The request received
     * @return BaseResponseContext object
     */
    public abstract BaseResponseContext handle(Request request);

    /**
     * Check if a request is valid in the context.
     * - does the resource support the HTTP method
     * - does the URL resource type match the body data type
     * - etc.
     *
     * @param methodType
     * @param resourceEndpointName
     * @param endpointRegistryEntry
     * @param bodyRegistryEntry
     */
    protected void verifyTypes(HttpMethod methodType,
                               String resourceEndpointName,
                               RegistryEntry endpointRegistryEntry,
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

    protected Serializable parseId(RegistryEntry registryEntry, String id) {
        @SuppressWarnings("unchecked") Class<? extends Serializable> idClass = (Class<? extends Serializable>) registryEntry
                .getResourceInformation()
                .getIdField()
                .getType();
        return getTypeParser().parse(id, idClass);
    }

    protected Iterable<? extends Serializable> parseIds(RegistryEntry registryEntry, Iterable<String> ids) {
        Class<? extends Serializable> idType = (Class<? extends Serializable>) registryEntry.getResourceInformation()
                .getIdField().getType();

        return getTypeParser().parse(ids, idType);
    }

    public RequestBody parseBody(InputStream inputStream) {
        try {
            return getObjectMapper().readValue(inputStream, RequestBody.class);
        } catch (IOException e) {
            throw new JsonSerializationException("Exception reading JSON API request body. " + e.getMessage());
        }
    }

}
