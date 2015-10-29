package io.katharsis.dispatcher.controller.collection;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.dispatcher.controller.resource.ResourceIncludeField;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.CollectionResponse;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;
import io.katharsis.utils.parser.TypeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

public class CollectionGet extends ResourceIncludeField {

    private static final transient Logger log = LoggerFactory.getLogger(CollectionGet.class);

    public CollectionGet(ResourceRegistry resourceRegistry, TypeParser typeParser, IncludeLookupSetter fieldSetter) {
        super(resourceRegistry, typeParser, fieldSetter);
    }

    /**
     * Check if it is a GET request for a collection of resources.
     */
    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return jsonPath.isCollection()
                && jsonPath instanceof ResourcePath
                && HttpMethod.GET.name().equals(requestType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public BaseResponse<?> handle(JsonPath jsonPath, QueryParams queryParams, RepositoryMethodParameterProvider 
        parameterProvider, RequestBody requestBody)
            throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        String resourceName = jsonPath.getElementName();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
        if (registryEntry == null) {
            throw new ResourceNotFoundException(resourceName);
        }
        Iterable<?> resources;
        ResourceRepository resourceRepository = registryEntry.getResourceRepository(parameterProvider);
        if (jsonPath.getIds() == null || jsonPath.getIds().getIds().isEmpty()) {
            resources = resourceRepository.findAll(queryParams);
        } else {
            Class<? extends Serializable> idType = (Class<? extends Serializable>)registryEntry
                .getResourceInformation().getIdField().getType();
            Iterable<? extends Serializable> parsedIds = typeParser.parse((Iterable<String>) jsonPath.getIds().getIds(),
                idType);
            resources = resourceRepository.findAll(parsedIds, queryParams);
        }

        List containers = new LinkedList();
        if (resources != null) {
            includeFieldSetter.setIncludedElements(resourceName, resources, queryParams, parameterProvider);
            for (Object element : resources) {
                containers.add(element);
            }
        }
        MetaInformation metaInformation = getMetaInformation(resourceRepository, resources, queryParams);
        LinksInformation linksInformation = getLinksInformation(resourceRepository, resources, queryParams);

        return new CollectionResponse(containers, jsonPath, queryParams, metaInformation, linksInformation);
    }
}
