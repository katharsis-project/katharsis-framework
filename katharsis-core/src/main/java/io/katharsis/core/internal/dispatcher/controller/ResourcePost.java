package io.katharsis.core.internal.dispatcher.controller;

import java.util.Collection;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.core.internal.dispatcher.path.JsonPath;
import io.katharsis.core.internal.dispatcher.path.ResourcePath;
import io.katharsis.core.internal.repository.adapter.ResourceRepositoryAdapter;
import io.katharsis.core.internal.resource.DocumentMapper;
import io.katharsis.errorhandling.exception.RequestBodyException;
import io.katharsis.errorhandling.exception.RequestBodyNotFoundException;
import io.katharsis.errorhandling.exception.ResourceNotFoundException;
import io.katharsis.legacy.internal.RepositoryMethodParameterProvider;
import io.katharsis.repository.request.HttpMethod;
import io.katharsis.repository.request.QueryAdapter;
import io.katharsis.repository.response.HttpStatus;
import io.katharsis.repository.response.JsonApiResponse;
import io.katharsis.repository.response.Response;
import io.katharsis.resource.Document;
import io.katharsis.resource.Resource;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.parser.TypeParser;

public class ResourcePost extends ResourceUpsert {

    public ResourcePost(ResourceRegistry resourceRegistry, TypeParser typeParser, ObjectMapper objectMapper, DocumentMapper documentMapper) {
        super(resourceRegistry, typeParser, objectMapper, documentMapper);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Check if it is a POST request for a resource.
     */
    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return jsonPath.isCollection() &&
            jsonPath instanceof ResourcePath &&
            HttpMethod.POST.name()
                .equals(requestType);
    }

    @Override
    public Response handle(JsonPath jsonPath, QueryAdapter queryAdapter,
                                          RepositoryMethodParameterProvider parameterProvider, Document document){
        String resourceEndpointName = jsonPath.getResourceName();
        RegistryEntry endpointRegistryEntry = resourceRegistry.getEntry(resourceEndpointName);
        if (endpointRegistryEntry == null) {
            throw new ResourceNotFoundException(resourceEndpointName);
        }
        if (document == null) {
            throw new RequestBodyNotFoundException(HttpMethod.POST, resourceEndpointName);
        }
        if (document.getData() instanceof Collection) {
            throw new RequestBodyException(HttpMethod.POST, resourceEndpointName, "Multiple data in body");
        }

        Resource resourceBody = (Resource) document.getData().get();
        if (resourceBody == null) {
            throw new RequestBodyException(HttpMethod.POST, resourceEndpointName, "No data field in the body.");
        }
        RegistryEntry bodyRegistryEntry = resourceRegistry.getEntry(resourceBody.getType());
        verifyTypes(HttpMethod.POST, resourceEndpointName, endpointRegistryEntry, bodyRegistryEntry);
        Object newResource = newResource(bodyRegistryEntry.getResourceInformation(), resourceBody);

        setId(resourceBody, newResource, bodyRegistryEntry.getResourceInformation());
        setAttributes(resourceBody, newResource, bodyRegistryEntry.getResourceInformation());
        ResourceRepositoryAdapter resourceRepository = endpointRegistryEntry.getResourceRepository(parameterProvider);
        setRelations(newResource, bodyRegistryEntry, resourceBody, queryAdapter, parameterProvider);
        
        JsonApiResponse apiResponse = resourceRepository.create(newResource, queryAdapter);
        if(apiResponse.getEntity() == null){
        	throw new IllegalStateException("repository did not return the created resource");
        }
        Set<String> loadedRelationshipNames = getLoadedRelationshipNames(resourceBody);
        Document responseDocument = documentMapper.toDocument(apiResponse, queryAdapter, parameterProvider, loadedRelationshipNames);

        return new Response(responseDocument, HttpStatus.CREATED_201);
    }
}
