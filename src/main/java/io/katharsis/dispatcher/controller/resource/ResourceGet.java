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

public class ResourceGet implements BaseController {

    private ResourceRegistry resourceRegistry;

    public ResourceGet(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    /**
     * Checks if requested resource method is acceptable
     *
     * @param resourcePath Requested resource path
     * @param requestType  HTTP request type
     * @return Acceptance result in boolean
     */
    @Override
    public boolean isAcceptable(ResourcePath resourcePath, String requestType) {
        return !resourcePath.isCollection() && "GET".equals(requestType);
    }

    /**
     * Passes the request to controller method
     *
     * @param resourcePath Requested resource path
     * @return ResourceResponse object
     */
    @Override
    public BaseResponse<?> handle(ResourcePath resourcePath) {
        String resourceName = resourcePath.getResourceName();
        PathIds resourceIds = resourcePath.getIds();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
        String id = resourceIds.getIds().get(0);

        Class<?> idType = registryEntry.getResourceInformation().getIdField().getType();
        Serializable castedId = castIdValue(id, idType);
        Object entity = registryEntry.getResourceRepository().findOne(castedId);

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
