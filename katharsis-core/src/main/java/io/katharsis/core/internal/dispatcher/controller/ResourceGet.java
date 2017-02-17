package io.katharsis.core.internal.dispatcher.controller;

import java.io.Serializable;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.core.internal.dispatcher.path.JsonPath;
import io.katharsis.core.internal.dispatcher.path.PathIds;
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
import io.katharsis.utils.Nullable;
import io.katharsis.utils.parser.TypeParser;

public class ResourceGet extends ResourceIncludeField {

	public ResourceGet(ResourceRegistry resourceRegistry, ObjectMapper objectMapper, TypeParser typeParser, DocumentMapper documentMapper) {
		super(resourceRegistry, objectMapper, typeParser, documentMapper);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Checks if requested resource method is acceptable - is a GET request for
	 * a resource.
	 */
	@Override
	public boolean isAcceptable(JsonPath jsonPath, String requestType) {
		return !jsonPath.isCollection() && jsonPath instanceof ResourcePath && HttpMethod.GET.name().equals(requestType);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Passes the request to controller method.
	 */
	@Override
	public Response handle(JsonPath jsonPath, QueryAdapter queryAdapter, RepositoryMethodParameterProvider parameterProvider, Document requestBody) {
		String resourceName = jsonPath.getElementName();
		PathIds resourceIds = jsonPath.getIds();
		RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
		if (registryEntry == null) {
			throw new ResourceNotFoundException(resourceName);
		}
		String id = resourceIds.getIds().get(0);

		@SuppressWarnings("unchecked")
		Class<? extends Serializable> idClass = (Class<? extends Serializable>) registryEntry.getResourceInformation().getIdField().getType();
		Serializable castedId = typeParser.parse(id, idClass);
		ResourceRepositoryAdapter resourceRepository = registryEntry.getResourceRepository(parameterProvider);
		JsonApiResponse entities = resourceRepository.findOne(castedId, queryAdapter);
		
		Document responseDocument = documentMapper.toDocument(entities, queryAdapter);
		
		// return explicit { data : null } if values found
		if(!responseDocument.getData().isPresent()){
			responseDocument.setData(Nullable.nullValue());
		}

		return new Response(responseDocument, 200);
	}

}
