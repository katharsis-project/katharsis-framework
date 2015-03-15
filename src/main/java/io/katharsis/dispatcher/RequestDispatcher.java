package io.katharsis.dispatcher;

import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.path.PathBuilder;
import io.katharsis.path.ResourcePath;

public class RequestDispatcher {

    private ControllerRegistry controllerRegistry;
    private PathBuilder pathBuilder;

    public RequestDispatcher(ControllerRegistry controllerRegistry, PathBuilder pathBuilder) {
        this.controllerRegistry = controllerRegistry;
        this.pathBuilder = pathBuilder;
    }

    public void dispatchRequest(String path, String requestType) {
        ResourcePath resourcePath = pathBuilder.buildPath(path);
        controllerRegistry.getController(resourcePath, requestType).handle(resourcePath);
    }
}
