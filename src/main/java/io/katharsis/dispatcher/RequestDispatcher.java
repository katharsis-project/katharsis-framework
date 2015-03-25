package io.katharsis.dispatcher;

import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.path.JsonPath;
import io.katharsis.response.BaseResponse;

public class RequestDispatcher {

    private ControllerRegistry controllerRegistry;

    public RequestDispatcher(ControllerRegistry controllerRegistry) {
        this.controllerRegistry = controllerRegistry;
    }

    public BaseResponse<?> dispatchRequest(JsonPath jsonPath, String requestType) {
        return controllerRegistry.getController(jsonPath, requestType).handle(jsonPath);
    }
}
