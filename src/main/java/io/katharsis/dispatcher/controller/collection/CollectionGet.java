package io.katharsis.dispatcher.controller.collection;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.CollectionResponse;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;
import io.katharsis.utils.parser.TypeParser;

import java.util.LinkedList;
import java.util.List;

public class CollectionGet implements BaseController {

    private final ResourceRegistry resourceRegistry;
    private final TypeParser typeParser;

    public CollectionGet(ResourceRegistry resourceRegistry, TypeParser typeParser) {
        this.resourceRegistry = resourceRegistry;
        this.typeParser = typeParser;
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
    public BaseResponse<?> handle(JsonPath jsonPath, RequestParams requestParams,
                                  RepositoryMethodParameterProvider parameterProvider, RequestBody requestBody) {
        String resourceName = jsonPath.getElementName();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
        if (registryEntry == null) {
            throw new ResourceNotFoundException(resourceName);
        }
        Iterable<?> resources;
        ResourceRepository resourceRepository = registryEntry.getResourceRepository(parameterProvider);
        resources = resourceRepository.findAll(requestParams);
        List containers = new LinkedList();
        if (resources != null) {
            for (Object element : resources) {
                containers.add(element);
            }
        }
        MetaInformation metaInformation = getMetaInformation(resourceRepository, resources, requestParams);
        LinksInformation linksInformation = getLinksInformation(resourceRepository, resources, requestParams);

        return new CollectionResponse(containers, jsonPath, requestParams, metaInformation, linksInformation);
    }
}
