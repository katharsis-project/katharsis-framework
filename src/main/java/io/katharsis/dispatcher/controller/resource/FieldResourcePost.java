package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.FieldPath;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathIds;
import io.katharsis.resource.exception.RequestBodyException;
import io.katharsis.resource.exception.RequestBodyNotFoundException;
import io.katharsis.resource.exception.ResourceFieldNotFoundException;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.RelationshipRepositoryAdapter;
import io.katharsis.resource.registry.responseRepository.ResourceRepositoryAdapter;
import io.katharsis.response.HttpStatus;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.ResourceResponseContext;
import io.katharsis.utils.Generics;
import io.katharsis.utils.PropertyUtils;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;
import java.util.Collections;

/**
 * Creates a new post in a similar manner as in {@link ResourcePost}, but additionally adds a relation to a field.
 */
public class FieldResourcePost extends ResourceUpsert {

    public FieldResourcePost(ResourceRegistry resourceRegistry, TypeParser typeParser, @SuppressWarnings
        ("SameParameterValue") ObjectMapper objectMapper) {
        super(resourceRegistry, typeParser, objectMapper);
    }

    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection()
            && FieldPath.class.equals(jsonPath.getClass())
            && HttpMethod.POST.name()
            .equals(requestType);
    }

    @Override
    public ResourceResponseContext handle(JsonPath jsonPath, QueryParams queryParams,
                                          RepositoryMethodParameterProvider parameterProvider, RequestBody requestBody) {
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

        RegistryEntry relationshipRegistryEntry = resourceRegistry.getEntry(relationshipFieldClass);
        String relationshipResourceType = resourceRegistry.getResourceType(relationshipFieldClass);

        DataBody dataBody = requestBody.getSingleData();
        Object resource = buildNewResource(relationshipRegistryEntry, dataBody, relationshipResourceType);
        setAttributes(dataBody, resource, relationshipRegistryEntry.getResourceInformation());
        ResourceRepositoryAdapter resourceRepository = relationshipRegistryEntry.getResourceRepository(parameterProvider);
        JsonApiResponse savedResourceResponse = resourceRepository.save(resource, queryParams);
        saveRelations(queryParams, extractResource(savedResourceResponse), relationshipRegistryEntry, dataBody, parameterProvider);

        Serializable resourceId = (Serializable) PropertyUtils
            .getProperty(extractResource(savedResourceResponse), relationshipRegistryEntry.getResourceInformation()
                .getIdField()
                .getUnderlyingName());

        RelationshipRepositoryAdapter relationshipRepositoryForClass = endpointRegistryEntry
            .getRelationshipRepositoryForClass(relationshipFieldClass, parameterProvider);

        @SuppressWarnings("unchecked")
        JsonApiResponse parent = endpointRegistryEntry.getResourceRepository(parameterProvider)
            .findOne(castedResourceId, queryParams);
        if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
            //noinspection unchecked
            relationshipRepositoryForClass.addRelations(parent.getEntity(), Collections.singletonList(resourceId), jsonPath
                .getElementName(), queryParams);
        } else {
            //noinspection unchecked
            relationshipRepositoryForClass.setRelation(parent.getEntity(), resourceId, jsonPath.getElementName(), queryParams);
        }
        return new ResourceResponseContext(savedResourceResponse, jsonPath, queryParams, HttpStatus.CREATED_201);
    }

    private Serializable getResourceId(PathIds resourceIds, RegistryEntry<?> registryEntry) {
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
