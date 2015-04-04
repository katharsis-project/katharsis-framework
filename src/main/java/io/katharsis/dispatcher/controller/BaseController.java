package io.katharsis.dispatcher.controller;

import io.katharsis.path.JsonPath;
import io.katharsis.response.BaseResponse;

/**
 * Represents a controller contract which all of the implementations must implements.
 */
public interface BaseController {

    /**
     * Checks if requested resource method is acceptable.
     *
     * @param jsonPath    Requested resource path
     * @param requestType HTTP request type
     * @return Acceptance result in boolean
     */
    boolean isAcceptable(JsonPath jsonPath, String requestType);

    /**
     * Passes the request to controller method.
     *
     * @param jsonPath Requested resource path
     * @return CollectionResponse object
     */
    BaseResponse<?> handle(JsonPath jsonPath);
}
