package io.katharsis.dispatcher.controller.collection;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.path.JsonPath;
import io.katharsis.path.PathBuilder;
import io.katharsis.path.ResourcePath;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.CollectionResponse;
import io.katharsis.response.Container;
import io.katharsis.response.TopLevelLinks;

import java.util.LinkedList;
import java.util.List;

public class CollectionGet implements BaseController {

    private ResourceRegistry resourceRegistry;
    private PathBuilder pathBuilder;

    public CollectionGet(ResourceRegistry resourceRegistry, PathBuilder pathBuilder) {
        this.resourceRegistry = resourceRegistry;
        this.pathBuilder = pathBuilder;
    }

    /**
     * {@inheritDoc}
     *
     * Check if it is a GET request for a collection of resources.
     */
    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return jsonPath.isCollection()
                && jsonPath instanceof ResourcePath
                && "GET".equals(requestType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // @TODO handle request params
    public BaseResponse<?> handle(JsonPath jsonPath, RequestParams requestParams) {
        String resourceName = jsonPath.getElementName();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
        if (registryEntry == null) {
            throw new ResourceNotFoundException("Resource of type not found: " + resourceName);
        }
        Iterable iterable = registryEntry.getResourceRepository().findAll();
        List<Container> containers = new LinkedList<>();
        for (Object element : iterable) {
            containers.add(new Container(element));
        }
        TopLevelLinks topLevelLinks = new TopLevelLinks(pathBuilder.buildPath(jsonPath));

        return new CollectionResponse(containers, topLevelLinks);
    }
}
