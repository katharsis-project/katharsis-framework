package io.katharsis.dispatcher.registry;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.path.JsonPath;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ControllerRegistry {

    private final List<BaseController> controllers = new LinkedList<>();

    public ControllerRegistry(BaseController... baseControllers) {
        if (baseControllers != null) {
            controllers.addAll(Arrays.asList(baseControllers));
        }
    }

    /**
     * Adds Katharsis controller to the registry.
     *
     * @param controller a controller to be added
     */
    public void addController(BaseController controller) {
        controllers.add(controller);
    }

    /**
     * Iterate over all registered controllers to get the first suitable one.
     * @param jsonPath built JsonPath object mad from request path
     * @param requestType type of a HTTP request
     * @return suitable controller
     */
    public BaseController getController(JsonPath jsonPath, String requestType) {
        for (BaseController controller : controllers) {
            if (controller.isAcceptable(jsonPath, requestType)) {
                return controller;
            }
        }
        // @todo Create custom exception
        throw new IllegalStateException("Matching controller not found");
    }
}
