package io.katharsis.dispatcher.registry;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathBuilder;

import java.util.LinkedList;
import java.util.List;

/**
 * Stores a list of controllers which are used to process the incoming requests.
 *
 * @see io.katharsis.dispatcher.RequestDispatcher
 */
public class ControllerRegistry {

    private final List<BaseController> controllers = new LinkedList<>();

    public ControllerRegistry(List<BaseController> baseControllers) {
        if (baseControllers != null) {
            controllers.addAll(baseControllers);
        }
    }

    /**
     * Adds Katharsis controller to the registry. Should be called at initialization time.
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
        throw new MethodNotFoundException(PathBuilder.buildPath(jsonPath), requestType);
    }
}
