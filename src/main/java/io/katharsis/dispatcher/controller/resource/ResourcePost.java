package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.path.JsonPath;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.request.DataBody;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.Container;
import io.katharsis.response.ResourceResponse;

public class ResourcePost implements BaseController {

    private ResourceRegistry resourceRegistry;

    public ResourcePost(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Check if it is a POST request for a resource.
     */
    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return jsonPath.isCollection() &&
                "POST".equals(requestType);
    }

    @Override
    public BaseResponse<?> handle(JsonPath jsonPath, RequestParams requestParams, DataBody requestBody) {
        String resourceName = jsonPath.getResourceName();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
        if (registryEntry == null) {
            throw new ResourceNotFoundException("Resource of type not found: " + resourceName);
        }

        Object resource = buildNewResource(requestBody, registryEntry);
        Object entityId = registryEntry.getResourceRepository().save(resource);

        return new ResourceResponse(new Container(entityId));
    }

    private Object buildNewResource(DataBody requestBody, RegistryEntry registryEntry) {
        return null;
    }
}
