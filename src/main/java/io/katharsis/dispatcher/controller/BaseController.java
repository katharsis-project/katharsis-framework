package io.katharsis.dispatcher.controller;

import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.MetaRepository;
import io.katharsis.repository.ResourceMetaRepository;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.MetaInformation;

/**
 * Represents a controller contract. There can be many kinds of requests that can be send to the framework. The
 * initial process of checking if a request is acceptable is managed by
 * {@link BaseController#isAcceptable(io.katharsis.request.path.JsonPath, String)} method. If the method returns
 * true, the matched controller is used to handle the request.
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
     * @throws Exception internal Katharsis exception
     */
    BaseResponse<?> handle(JsonPath jsonPath, RequestParams requestParams, RequestBody requestBody) throws Exception;

    default MetaInformation getMetaInformation(Object repository, Iterable<?> resources) {
        if (repository instanceof ResourceMetaRepository) {
            return ((ResourceMetaRepository) repository).getMetaInformation(resources);
        }
        return null;
    }

    default MetaInformation getMetaInformation(Object repository) {
        if (repository instanceof MetaRepository) {
            return ((MetaRepository) repository).getMetaInformation();
        }
        return null;
    }
}
