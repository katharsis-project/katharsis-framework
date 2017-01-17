package io.katharsis.dispatcher.controller.resource;

import java.util.Collection;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.dispatcher.controller.Response;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.Document;
import io.katharsis.resource.Resource;
import io.katharsis.resource.exception.RequestBodyException;
import io.katharsis.resource.exception.RequestBodyNotFoundException;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.internal.DocumentMapper;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.repository.adapter.ResourceRepositoryAdapter;
import io.katharsis.response.HttpStatus;
import io.katharsis.response.JsonApiResponse;
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
        Document responseDocument = documentMapper.toDocument(apiResponse, queryAdapter, parameterProvider);

        return new Response(responseDocument, HttpStatus.CREATED_201);
    }
}
