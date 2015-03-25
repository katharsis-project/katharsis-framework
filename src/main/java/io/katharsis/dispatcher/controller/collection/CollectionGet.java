package io.katharsis.dispatcher.controller.collection;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.path.JsonPath;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.CollectionResponse;
import io.katharsis.response.Container;

import java.util.LinkedList;
import java.util.List;

public class CollectionGet implements BaseController {

    private ResourceRegistry resourceRegistry;

    public CollectionGet(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    /**
     * Checks if requested resource method is acceptable
     *
     * @param jsonPath Requested resource path
     * @param requestType  HTTP request type
     * @return Acceptance result in boolean
     */
    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return jsonPath.isCollection()
                && !jsonPath.isRelationship()
                && "GET".equals(requestType);
    }

    /**
     * Passes the request to controller method
     *
     * @param jsonPath Requested resource path
     * @return CollectionResponse object
     */
    @Override
    public BaseResponse<?> handle(JsonPath jsonPath) {
        String resourceName = jsonPath.getElementName();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
        Iterable iterable = registryEntry.getResourceRepository().findAll();
        List<Container> containers = new LinkedList<>();
        for (Object element : iterable) {
            containers.add(new Container(element));
        }

        return new CollectionResponse(containers);
    }
}
