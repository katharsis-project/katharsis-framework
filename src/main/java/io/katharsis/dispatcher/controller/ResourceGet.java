package io.katharsis.dispatcher.controller;

import io.katharsis.path.PathIds;
import io.katharsis.path.ResourcePath;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.ResourceResponse;

public class ResourceGet implements BaseController {

    private ResourceRegistry resourceRegistry;

    public ResourceGet(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public boolean isAcceptable(ResourcePath resourcePath, String requestType) {
        return !resourcePath.isCollection() && "GET".equals(requestType);
    }

    @Override
    public BaseResponse<?> handle(ResourcePath resourcePath) {
        String resourceName = resourcePath.getResourceName();
        PathIds resourceIds = resourcePath.getIds();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
        String id = resourceIds.getIds().get(0);

        // @TODO add automatic casting of ids
        Object entity = registryEntry.getEntityRepository().findOne(Long.valueOf(id));

        return new ResourceResponse<>(entity);
    }
}
