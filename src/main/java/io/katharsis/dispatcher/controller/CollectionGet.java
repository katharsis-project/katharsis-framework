package io.katharsis.dispatcher.controller;

import io.katharsis.path.ResourcePath;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.CollectionResponse;

public class CollectionGet implements BaseController {

    private ResourceRegistry resourceRegistry;

    public CollectionGet(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public boolean isAcceptable(ResourcePath resourcePath, String requestType) {
        return resourcePath.isCollection() && "GET".equals(requestType);
    }

    @Override
    public BaseResponse<?> handle(ResourcePath resourcePath) {
        String resourceName = resourcePath.getResourceName();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
        Iterable iterable = registryEntry.getEntityRepository().findAll();

        return new CollectionResponse(iterable);
    }
}
