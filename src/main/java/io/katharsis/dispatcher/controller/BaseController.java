package io.katharsis.dispatcher.controller;

import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.queryParams.RequestParams;
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
     * @param requestParams Params specifying request
     * @param requestBody Top-level JSON object from method's body of the request passed as {@link RequestBody}
     * @return CollectionResponse object
     */
    BaseResponse<?> handle(JsonPath jsonPath, RequestParams requestParams, RequestBody requestBody) throws Exception;
}
