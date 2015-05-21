package io.katharsis.dispatcher.controller.resource;

import io.katharsis.queryParams.RequestParams;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.Container;
import io.katharsis.response.ResourceResponse;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;

public class ResourcePatch extends ResourceUpsert {

    public ResourcePatch(ResourceRegistry resourceRegistry, TypeParser typeParser) {
        super(resourceRegistry, typeParser);
    }

    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection() &&
                jsonPath instanceof ResourcePath &&
                "PATCH".equals(requestType);
    }

    @Override
    public BaseResponse<?> handle(JsonPath jsonPath, RequestParams requestParams, RequestBody requestBody) throws Exception {
        String resourceName = jsonPath.getResourceName();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
        if (registryEntry == null) {
            throw new ResourceNotFoundException("Resource of type not found: " + resourceName);
        }
        if (requestBody == null) {
            throw new RuntimeException("No body provided");
        }

        String idString = jsonPath.getIds().getIds().get(0);

        Class<? extends Serializable> idClass = (Class<? extends Serializable>) registryEntry
                .getResourceInformation()
                .getIdField()
                .getType();
        Serializable resourceId = typeParser.parse(idString, idClass);

        Object resource = registryEntry.getResourceRepository().findOne(resourceId);
        setAttributes(requestBody, resource, registryEntry.getResourceInformation());
        Object savedResource = registryEntry.getResourceRepository().save(resource);
        saveRelations(savedResource, registryEntry, requestBody);

        Object savedResourceWithRelations = registryEntry.getResourceRepository().findOne(resourceId);

        return new ResourceResponse(new Container(savedResourceWithRelations));
    }
}
