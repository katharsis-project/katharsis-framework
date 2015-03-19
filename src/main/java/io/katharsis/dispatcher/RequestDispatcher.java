package io.katharsis.dispatcher;

import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.path.ResourcePath;
import io.katharsis.response.BaseResponse;

public class RequestDispatcher {

    private ControllerRegistry controllerRegistry;

    public RequestDispatcher(ControllerRegistry controllerRegistry) {
        this.controllerRegistry = controllerRegistry;
    }

    public BaseResponse<?> dispatchRequest(ResourcePath resourcePath, String requestType) {
        return controllerRegistry.getController(resourcePath, requestType).handle(resourcePath);
    }
}
