package io.katharsis.dispatcher;

import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistry;
import io.katharsis.errorhandling.mapper.JsonApiExceptionMapper;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.utils.java.Optional;

/**
 * A class that can be used to integrate Katharsis with external frameworks like Jersey, Spring etc. See katharsis-rs
 * and katharsis-servlet for usage.
 */
public class RequestDispatcher {

    private final ControllerRegistry controllerRegistry;
    private final ExceptionMapperRegistry exceptionMapperRegistry;

    public RequestDispatcher(ControllerRegistry controllerRegistry, ExceptionMapperRegistry exceptionMapperRegistry) {
        this.controllerRegistry = controllerRegistry;
        this.exceptionMapperRegistry = exceptionMapperRegistry;
    }

    /**
     * Dispatch the request from a client
     *
     * @param jsonPath          built {@link JsonPath} instance which represents the URI sent in the request
     * @param requestType       type of the request e.g. POST, GET, PATCH
     * @param parameterProvider repository method parameter provider
     * @param queryParams       built object containing query parameters of the request
     * @param requestBody       deserialized body of the client request
     * @return the response form the Katharsis
     */
    public BaseResponseContext dispatchRequest(JsonPath jsonPath, String requestType, QueryParams queryParams,
                                                  RepositoryMethodParameterProvider parameterProvider,
                                                  @SuppressWarnings("SameParameterValue") RequestBody requestBody) {

        try {
            return controllerRegistry
                .getController(jsonPath, requestType)
                .handle(jsonPath, queryParams, parameterProvider, requestBody);
        } catch (Exception e) {
            Optional<JsonApiExceptionMapper> exceptionMapper = exceptionMapperRegistry.findMapperFor(e.getClass());
            if (exceptionMapper.isPresent()) {
                //noinspection unchecked
                return exceptionMapper.get()
                    .toErrorResponse(e);
            } else {
                throw e;
            }
        }
    }
}
