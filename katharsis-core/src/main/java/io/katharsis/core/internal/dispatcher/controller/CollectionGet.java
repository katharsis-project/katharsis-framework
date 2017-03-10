package io.katharsis.core.internal.dispatcher.controller;

import java.io.Serializable;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.core.internal.dispatcher.path.JsonPath;
import io.katharsis.core.internal.dispatcher.path.ResourcePath;
import io.katharsis.core.internal.repository.adapter.ResourceRepositoryAdapter;
import io.katharsis.core.internal.resource.DocumentMapper;
import io.katharsis.errorhandling.exception.ResourceNotFoundException;
import io.katharsis.legacy.internal.RepositoryMethodParameterProvider;
import io.katharsis.repository.request.HttpMethod;
import io.katharsis.repository.request.QueryAdapter;
import io.katharsis.repository.response.JsonApiResponse;
import io.katharsis.repository.response.Response;
import io.katharsis.resource.Document;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.parser.TypeParser;

public class CollectionGet extends ResourceIncludeField {

	public CollectionGet(ResourceRegistry resourceRegistry, ObjectMapper objectMapper, TypeParser typeParser, DocumentMapper documentMapper) {
        super(resourceRegistry, objectMapper, typeParser, documentMapper);
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
    public Response handle(JsonPath jsonPath, QueryAdapter queryAdapter, RepositoryMethodParameterProvider
        parameterProvider, Document requestBody) {
        String resourceName = jsonPath.getElementName();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
        if (registryEntry == null) {
            throw new ResourceNotFoundException(resourceName);
        }
        Document responseDocument;
        ResourceRepositoryAdapter resourceRepository = registryEntry.getResourceRepository(parameterProvider);
        JsonApiResponse entities;
		if (jsonPath.getIds() == null || jsonPath.getIds().getIds().isEmpty()) {
            entities = resourceRepository.findAll(queryAdapter);
        } else {
            Class<? extends Serializable> idType = (Class<? extends Serializable>)registryEntry
                .getResourceInformation().getIdField().getType();
            Iterable<? extends Serializable> parsedIds = typeParser.parse((Iterable<String>) jsonPath.getIds().getIds(),
                idType);
            entities = resourceRepository.findAll(parsedIds, queryAdapter);
        }
        responseDocument = documentMapper.toDocument(entities, queryAdapter, parameterProvider);
        
        return new Response(responseDocument, 200);
    }
}
