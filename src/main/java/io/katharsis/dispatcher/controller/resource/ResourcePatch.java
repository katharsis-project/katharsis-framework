package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.exception.RequestBodyException;
import io.katharsis.resource.exception.RequestBodyNotFoundException;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
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
                HttpMethod.PATCH.name().equals(requestType);
    }

    @Override
    public BaseResponse<?> handle(JsonPath jsonPath, RequestParams requestParams, RequestBody requestBody) throws Exception {
        String resourceName = jsonPath.getResourceName();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
        if (registryEntry == null) {
            throw new ResourceNotFoundException(resourceName);
        }
        if (requestBody == null) {
            throw new RequestBodyNotFoundException(HttpMethod.PATCH, resourceName);
        }
        if (requestBody.isMultiple()) {
            throw new RequestBodyException(HttpMethod.POST, resourceName, "Multiple data in body");
        }


        String idString = jsonPath.getIds().getIds().get(0);

        Class<? extends Serializable> idClass = (Class<? extends Serializable>) registryEntry
                .getResourceInformation()
                .getIdField()
                .getType();
        Serializable resourceId = typeParser.parse(idString, idClass);

        Object resource = registryEntry.getResourceRepository().findOne(resourceId, requestParams);
        DataBody dataBody = requestBody.getSingleData();

        setAttributes(dataBody, resource, registryEntry.getResourceInformation());
        Object savedResource = registryEntry.getResourceRepository().save(resource);
        saveRelations(savedResource, registryEntry, dataBody);

        Object savedResourceWithRelations = registryEntry.getResourceRepository().findOne(resourceId, requestParams);

        return new ResourceResponse(savedResourceWithRelations, jsonPath, requestParams);
    }
}
