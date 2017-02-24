package io.katharsis.core.internal.dispatcher.controller;

import java.io.Serializable;
import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.core.internal.dispatcher.path.FieldPath;
import io.katharsis.core.internal.dispatcher.path.JsonPath;
import io.katharsis.core.internal.dispatcher.path.PathIds;
import io.katharsis.core.internal.repository.adapter.RelationshipRepositoryAdapter;
import io.katharsis.core.internal.repository.adapter.ResourceRepositoryAdapter;
import io.katharsis.core.internal.resource.DocumentMapper;
import io.katharsis.core.internal.utils.Generics;
import io.katharsis.errorhandling.exception.RequestBodyException;
import io.katharsis.errorhandling.exception.RequestBodyNotFoundException;
import io.katharsis.errorhandling.exception.ResourceFieldNotFoundException;
import io.katharsis.errorhandling.exception.ResourceNotFoundException;
import io.katharsis.legacy.internal.RepositoryMethodParameterProvider;
import io.katharsis.repository.request.HttpMethod;
import io.katharsis.repository.request.QueryAdapter;
import io.katharsis.repository.response.HttpStatus;
import io.katharsis.repository.response.JsonApiResponse;
import io.katharsis.repository.response.Response;
import io.katharsis.resource.Document;
import io.katharsis.resource.Resource;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.parser.TypeParser;

/**
 * Creates a new post in a similar manner as in {@link ResourcePost}, but additionally adds a relation to a field.
 */
public class FieldResourcePost extends ResourceUpsert {

    public FieldResourcePost(ResourceRegistry resourceRegistry, TypeParser typeParser, @SuppressWarnings
        ("SameParameterValue") ObjectMapper objectMapper, DocumentMapper documentMapper) {
        super(resourceRegistry, typeParser, objectMapper, documentMapper);
    }

    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection()
            && FieldPath.class.equals(jsonPath.getClass())
            && HttpMethod.POST.name()
            .equals(requestType);
    }

    @Override
    public Response handle(JsonPath jsonPath, QueryAdapter queryAdapter,
                                          RepositoryMethodParameterProvider parameterProvider, Document requestBody) {
        String resourceEndpointName = jsonPath.getResourceName();
        PathIds resourceIds = jsonPath.getIds();
        RegistryEntry endpointRegistryEntry = resourceRegistry.getEntry(resourceEndpointName);

        if (endpointRegistryEntry == null) {
            throw new ResourceNotFoundException(resourceEndpointName);
        }
        if (requestBody == null) {
            throw new RequestBodyNotFoundException(HttpMethod.POST, resourceEndpointName);
        }
        if (requestBody.isMultiple()) {
            throw new RequestBodyException(HttpMethod.POST, resourceEndpointName, "Multiple data in body");
        }

        Serializable castedResourceId = getResourceId(resourceIds, endpointRegistryEntry);
        ResourceField relationshipField = endpointRegistryEntry.getResourceInformation()
            .findRelationshipFieldByName(jsonPath.getElementName());
        if (relationshipField == null) {
            throw new ResourceFieldNotFoundException(jsonPath.getElementName());
        }

        Class<?> baseRelationshipFieldClass = relationshipField.getType();
        Class<?> relationshipFieldClass = Generics
            .getResourceClass(relationshipField.getGenericType(), baseRelationshipFieldClass);

        RegistryEntry relationshipRegistryEntry = resourceRegistry.findEntry(relationshipFieldClass);
        String relationshipResourceType = relationshipField.getOppositeResourceType();

        Resource dataBody = (Resource) requestBody.getData().get();
        Object resource = buildNewResource(relationshipRegistryEntry, dataBody, relationshipResourceType);
        setAttributes(dataBody, resource, relationshipRegistryEntry.getResourceInformation());
        ResourceRepositoryAdapter resourceRepository = relationshipRegistryEntry.getResourceRepository(parameterProvider);
        Document savedResourceResponse = documentMapper.toDocument(resourceRepository.create(resource, queryAdapter), queryAdapter, parameterProvider);
        saveRelations(queryAdapter, extractResource(savedResourceResponse), relationshipRegistryEntry, dataBody, parameterProvider);

        Serializable resourceId = relationshipRegistryEntry.getResourceInformation().parseIdString(savedResourceResponse.getSingleData().get().getId());

        RelationshipRepositoryAdapter relationshipRepositoryForClass = endpointRegistryEntry
            .getRelationshipRepositoryForClass(relationshipFieldClass, parameterProvider);

        @SuppressWarnings("unchecked")
        JsonApiResponse parent = endpointRegistryEntry.getResourceRepository(parameterProvider)
            .findOne(castedResourceId, queryAdapter);
        if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
            //noinspection unchecked
            relationshipRepositoryForClass.addRelations(parent.getEntity(), Collections.singletonList(resourceId), relationshipField, queryAdapter);
        } else {
            //noinspection unchecked
            relationshipRepositoryForClass.setRelation(parent.getEntity(), resourceId, relationshipField, queryAdapter);
        }
        return new Response(savedResourceResponse, HttpStatus.CREATED_201);
    }

    private Serializable getResourceId(PathIds resourceIds, RegistryEntry registryEntry) {
        String resourceId = resourceIds.getIds()
            .get(0);
        @SuppressWarnings("unchecked")
        Class<? extends Serializable> idClass = (Class<? extends Serializable>) registryEntry
            .getResourceInformation()
            .getIdField()
            .getType();
        return typeParser.parse(resourceId, idClass);
    }
}
