package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.path.PathIds;
import io.katharsis.path.ResourcePath;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.Container;
import io.katharsis.response.ResourceResponse;

import java.io.Serializable;

public class ResourcePost implements BaseController {

    private ResourceRegistry resourceRegistry;

    public ResourcePost(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public boolean isAcceptable(ResourcePath resourcePath, String requestType) {
        return !resourcePath.isCollection() && "POST".equals(requestType);
    }

    @Override
    public BaseResponse<?> handle(ResourcePath resourcePath) {
        String resourceName = resourcePath.getResourceName();
        PathIds resourceIds = resourcePath.getIds();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
        String id = resourceIds.getIds().get(0);

        Class<?> idType = registryEntry.getResourceInformation().getIdField().getType();
        Serializable castedId = castIdValue(id, idType);
        Object entity = registryEntry.getResourceRepository().save(castedId);

        return new ResourceResponse<>(new Container(entity));
    }

    // @TODO add more customized casting of ids
    private Serializable castIdValue(String id, Class<?> idType) {
        if (Long.class == idType) {
            return Long.valueOf(id);
        }
        return id;
    }
}
