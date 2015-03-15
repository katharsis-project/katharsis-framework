package io.katharsis.dispatcher.registry;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.path.ResourcePath;

import java.util.LinkedList;
import java.util.List;

public class ControllerRegistry {

    private final List<BaseController> controllers = new LinkedList<>();

    public void addController(BaseController controller) {
        controllers.add(controller);
    }

    public BaseController getController(ResourcePath resourcePath, String requestType) {
        for (BaseController controller : controllers) {
            if (controller.isAcceptable(resourcePath, requestType)) {
                return controller;
            }
        }
        // @todo Create custom exception
        throw new IllegalStateException("Matching controller not found");
    }
}
