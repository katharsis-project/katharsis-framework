package io.katharsis.dispatcher;

import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.queryParams.RequestParams;
import io.katharsis.response.BaseResponse;

public class RequestDispatcher {

    private ControllerRegistry controllerRegistry;

    public RequestDispatcher(ControllerRegistry controllerRegistry) {
        this.controllerRegistry = controllerRegistry;
    }

    public BaseResponse<?> dispatchRequest(JsonPath jsonPath, String requestType, RequestParams requestParams,
                                           RequestBody requestBody) throws Exception {
        return controllerRegistry
                .getController(jsonPath, requestType)
                .handle(jsonPath, requestParams, requestBody);
    }
}
