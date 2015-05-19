package io.katharsis.dispatcher;

import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistry;
import io.katharsis.errorhandling.mapper.JsonApiExceptionMapper;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.response.BaseResponse;

import java.util.Optional;

public class RequestDispatcher {

    private final ControllerRegistry controllerRegistry;
    private final ExceptionMapperRegistry exceptionMapperRegistry;

    public RequestDispatcher(ControllerRegistry controllerRegistry, ExceptionMapperRegistry exceptionMapperRegistry) {
        this.controllerRegistry = controllerRegistry;
        this.exceptionMapperRegistry = exceptionMapperRegistry;
    }

    public BaseResponse<?> dispatchRequest(JsonPath jsonPath, String requestType, RequestParams requestParams,
                                           RequestBody requestBody) throws Exception {

        try {
        return controllerRegistry
                .getController(jsonPath, requestType)
                .handle(jsonPath, requestParams, requestBody);
        } catch (Exception e) {
            Optional<JsonApiExceptionMapper> exceptionMapper = exceptionMapperRegistry.findMapperFor(e.getClass());
            if (exceptionMapper.isPresent()) {
                return exceptionMapper.get().toErrorResponse(e);
            } else {
                throw e;
            }
        }
    }
}
