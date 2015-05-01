package io.katharsis.dispatcher;

import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.path.JsonPath;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.request.DataBody;
import io.katharsis.response.BaseResponse;

public class RequestDispatcher {

    private ControllerRegistry controllerRegistry;

    public RequestDispatcher(ControllerRegistry controllerRegistry) {
        this.controllerRegistry = controllerRegistry;
    }

    public BaseResponse<?> dispatchRequest(JsonPath jsonPath, String requestType, RequestParams requestParams,
                                           DataBody requestBody) {
        return controllerRegistry
                .getController(jsonPath, requestType)
                .handle(jsonPath, requestParams, requestBody);
    }
}
