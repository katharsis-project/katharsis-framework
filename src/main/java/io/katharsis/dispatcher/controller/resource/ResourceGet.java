package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathIds;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;
import io.katharsis.response.ResourceResponse;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

public class ResourceGet extends ResourceIncludeField {

    public ResourceGet(ResourceRegistry resourceRegistry, TypeParser typeParser, IncludeLookupSetter fieldSetter) {
        super(resourceRegistry, typeParser, fieldSetter);
    }

    /**
     * {@inheritDoc}
     *
     * Checks if requested resource method is acceptable - is a GET request for a resource.
     */
    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection()
                && jsonPath instanceof ResourcePath
                && HttpMethod.GET.name().equals(requestType);
    }

    /**
     * {@inheritDoc}
     *
     * Passes the request to controller method.
     */
    @Override
    public BaseResponse<?> handle(JsonPath jsonPath, QueryParams queryParams, RepositoryMethodParameterProvider 
        parameterProvider, RequestBody requestBody)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        String resourceName = jsonPath.getElementName();
        PathIds resourceIds = jsonPath.getIds();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
        if (registryEntry == null) {
            throw new ResourceNotFoundException(resourceName);
        }
        String id = resourceIds.getIds().get(0);

        @SuppressWarnings("unchecked") Class<? extends Serializable> idClass = (Class<? extends Serializable>) registryEntry
                .getResourceInformation()
                .getIdField()
                .getType();
        Serializable castedId = typeParser.parse(id, idClass);
        ResourceRepository resourceRepository = registryEntry.getResourceRepository(parameterProvider);
        @SuppressWarnings("unchecked")
        Object entity = resourceRepository.findOne(castedId, queryParams);
        MetaInformation metaInformation =
            getMetaInformation(resourceRepository, Collections.singletonList(entity), queryParams);
        LinksInformation linksInformation =
            getLinksInformation(resourceRepository, Collections.singletonList(entity), queryParams);
        includeFieldSetter.setIncludedElements(resourceName, entity, queryParams, parameterProvider);

        return new ResourceResponse(entity, jsonPath, queryParams, metaInformation, linksInformation);
    }
}
