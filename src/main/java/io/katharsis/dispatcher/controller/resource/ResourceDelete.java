package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathIds;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

public class ResourceDelete implements BaseController {

    private ResourceRegistry resourceRegistry;
    private TypeParser typeParser;

    public ResourceDelete(ResourceRegistry resourceRegistry, TypeParser typeParser) {
        this.resourceRegistry = resourceRegistry;
        this.typeParser = typeParser;
    }

    /**
     * {@inheritDoc}
     *
     * Checks if requested resource method is acceptable - is a DELETE request for a resource.
     */
    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection()
                && jsonPath instanceof ResourcePath
                && HttpMethod.DELETE.name().equals(requestType);
    }

    @Override
    public BaseResponse<?> handle(JsonPath jsonPath, RequestParams requestParams, RequestBody requestBody)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String resourceName = jsonPath.getElementName();
        PathIds resourceIds = jsonPath.getIds();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
        if (registryEntry == null) {
            //TODO: Add JsonPath toString and provide to exception?
            throw new ResourceNotFoundException(resourceName);
        }
        for (String id : resourceIds.getIds()) {
            Class<? extends Serializable> idClass = (Class<? extends Serializable>) registryEntry
                    .getResourceInformation()
                    .getIdField()
                    .getType();
            Serializable castedId = typeParser.parse(id, idClass);
            registryEntry.getResourceRepository().delete(castedId);
        }

        //TODO: Avoid nulls - use optional
        return null;
    }
}
