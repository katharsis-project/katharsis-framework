package io.katharsis.dispatcher.controller.resource;

import java.io.Serializable;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.dispatcher.controller.Response;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathIds;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.Document;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.repository.adapter.ResourceRepositoryAdapter;
import io.katharsis.utils.parser.TypeParser;

public class ResourceGet extends ResourceIncludeField {

	public ResourceGet(ResourceRegistry resourceRegistry, ObjectMapper objectMapper, TypeParser typeParser, IncludeLookupSetter fieldSetter) {
		super(resourceRegistry, objectMapper, typeParser, fieldSetter);
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
		@SuppressWarnings("unchecked")
		Document responseDocument = documentMapper.toDocument(resourceRepository.findOne(castedId, queryAdapter));
		includeFieldSetter.setIncludedElements(resourceName, responseDocument, queryAdapter, parameterProvider);

		return new Response(responseDocument, 200);
	}

}
