package io.katharsis.core.internal.dispatcher.controller;

import java.io.Serializable;
import java.util.Collections;

import io.katharsis.core.internal.dispatcher.path.JsonPath;
import io.katharsis.core.internal.dispatcher.path.PathIds;
import io.katharsis.core.internal.dispatcher.path.RelationshipsPath;
import io.katharsis.core.internal.repository.adapter.RelationshipRepositoryAdapter;
import io.katharsis.core.internal.repository.adapter.ResourceRepositoryAdapter;
import io.katharsis.core.internal.utils.Generics;
import io.katharsis.errorhandling.exception.RequestBodyException;
import io.katharsis.errorhandling.exception.RequestBodyNotFoundException;
import io.katharsis.errorhandling.exception.ResourceFieldNotFoundException;
import io.katharsis.errorhandling.exception.ResourceNotFoundException;
import io.katharsis.legacy.internal.RepositoryMethodParameterProvider;
import io.katharsis.repository.request.HttpMethod;
import io.katharsis.repository.request.QueryAdapter;
import io.katharsis.repository.response.HttpStatus;
import io.katharsis.repository.response.Response;
import io.katharsis.resource.Document;
import io.katharsis.resource.ResourceIdentifier;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.parser.TypeParser;

public abstract class RelationshipsResourceUpsert extends BaseController {

    private final ResourceRegistry resourceRegistry;
    final TypeParser typeParser;

    RelationshipsResourceUpsert(ResourceRegistry resourceRegistry, TypeParser typeParser) {
        this.resourceRegistry = resourceRegistry;
        this.typeParser = typeParser;
    }

    /**
     * HTTP method name
     *
     * @return HTTP method name
     */
    protected abstract HttpMethod method();

    /**
     * Processes To-Many field
     *
     * @param resource                       source resource
     * @param relationshipIdType             {@link Class} class of the relationship's id field
     * @param elementName                    field's name
     * @param dataBodies                     Data bodies with relationships
     * @param queryAdapter                   QueryAdapter
     * @param relationshipRepositoryForClass Relationship repository
     */
    protected abstract void processToManyRelationship(Object resource, Class<? extends Serializable> relationshipIdType,
    												  ResourceField resourceField, Iterable<ResourceIdentifier> dataBodies, QueryAdapter queryAdapter,
                                                      RelationshipRepositoryAdapter relationshipRepositoryForClass);

    /**
     * Processes To-One field
     *
     * @param resource                       source resource
     * @param relationshipIdType             {@link Class} class of the relationship's id field
     * @param elementName                    field's name
     * @param dataBody                       Data body with a relationship
     * @param queryAdapter                   QueryAdapter
     * @param relationshipRepositoryForClass Relationship repository
     */
    protected abstract void processToOneRelationship(Object resource, Class<? extends Serializable> relationshipIdType,
                                                     ResourceField resourceField, ResourceIdentifier dataBody, QueryAdapter queryAdapter,
                                                     RelationshipRepositoryAdapter relationshipRepositoryForClass);

    @Override
    public final boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection()
                && RelationshipsPath.class.equals(jsonPath.getClass())
                && method().name().equals(requestType);
    }

    @Override
    public final Response handle(JsonPath jsonPath, QueryAdapter queryAdapter,
                                            RepositoryMethodParameterProvider parameterProvider, Document requestBody) {
        String resourceName = jsonPath.getResourceName();
        PathIds resourceIds = jsonPath.getIds();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);

        if (registryEntry == null) {
            throw new ResourceNotFoundException(resourceName);
        }
        if (requestBody == null) {
            throw new RequestBodyNotFoundException(HttpMethod.POST, resourceName);
        }

        Serializable castedResourceId = getResourceId(resourceIds, registryEntry);
        ResourceField relationshipField = registryEntry.getResourceInformation().findRelationshipFieldByName(jsonPath
                .getElementName());
        if (relationshipField == null) {
            throw new ResourceFieldNotFoundException(jsonPath.getElementName());
        }
        ResourceRepositoryAdapter resourceRepository = registryEntry.getResourceRepository(parameterProvider);
        @SuppressWarnings("unchecked")
        Object resource = extractResource(resourceRepository.findOne(castedResourceId, queryAdapter));

        Class<?> baseRelationshipFieldClass = relationshipField.getType();
        Class<?> relationshipFieldClass = Generics
                .getResourceClass(relationshipField.getGenericType(), baseRelationshipFieldClass);
        @SuppressWarnings("unchecked") Class<? extends Serializable> relationshipIdType = (Class<? extends Serializable>) resourceRegistry
                .findEntry(relationshipFieldClass).getResourceInformation().getIdField().getType();

        @SuppressWarnings("unchecked")
        RelationshipRepositoryAdapter relationshipRepositoryForClass = registryEntry
                .getRelationshipRepositoryForClass(relationshipFieldClass, parameterProvider);
        if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
            Iterable<ResourceIdentifier> dataBodies = (Iterable<ResourceIdentifier>) (requestBody.isMultiple() ? requestBody.getData().get() : Collections.singletonList(requestBody.getData().get()));
            processToManyRelationship(resource, relationshipIdType, relationshipField, dataBodies, queryAdapter,
                    relationshipRepositoryForClass);
        } else {
            if (requestBody.isMultiple()) {
                throw new RequestBodyException(HttpMethod.POST, resourceName, "Multiple data in body");
            }
            ResourceIdentifier dataBody = (ResourceIdentifier) requestBody.getData().get();
            processToOneRelationship(resource, relationshipIdType, relationshipField, dataBody, queryAdapter,
                    relationshipRepositoryForClass);
        }

        return new Response(new Document(), HttpStatus.NO_CONTENT_204);
    }

    private Serializable getResourceId(PathIds resourceIds, RegistryEntry registryEntry) {
        String resourceId = resourceIds.getIds().get(0);
        @SuppressWarnings("unchecked") Class<? extends Serializable> idClass = (Class<? extends Serializable>) registryEntry
                .getResourceInformation()
                .getIdField()
                .getType();
        return typeParser.parse(resourceId, idClass);
    }
}
