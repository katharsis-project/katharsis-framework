package io.katharsis.dispatcher.controller.relationship;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.dispatcher.controller.resource.ResourceIncludeField;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathIds;
import io.katharsis.request.path.RelationshipsPath;
import io.katharsis.resource.exception.ResourceFieldNotFoundException;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.RelationshipRepositoryAdapter;
import io.katharsis.response.*;
import io.katharsis.utils.Generics;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RelationshipsResourceHead extends RelationshipsResourceGet {

    public RelationshipsResourceHead(ResourceRegistry resourceRegistry, TypeParser typeParser, IncludeLookupSetter fieldSetter) {
        super(resourceRegistry, typeParser, fieldSetter);
    }

    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection()
            && jsonPath instanceof RelationshipsPath
            && HttpMethod.HEAD.name().equals(requestType);
    }

    @Override
    public BaseResponseContext handle(JsonPath jsonPath, QueryParams queryParams,
                                      RepositoryMethodParameterProvider parameterProvider, RequestBody requestBody) {
        String resourceName = jsonPath.getResourceName();
        PathIds resourceIds = jsonPath.getIds();
        RegistryEntry<?> registryEntry = resourceRegistry.getEntry(resourceName);

        Serializable castedResourceId = getResourceId(resourceIds, registryEntry);
        String elementName = jsonPath.getElementName();
        ResourceField relationshipField = registryEntry.getResourceInformation()
            .findRelationshipFieldByName(elementName);
        if (relationshipField == null) {
            throw new ResourceFieldNotFoundException(elementName);
        }

        Class<?> baseRelationshipFieldClass = relationshipField.getType();
        Class<?> relationshipFieldClass = Generics
            .getResourceClass(relationshipField.getGenericType(), baseRelationshipFieldClass);

        RelationshipRepositoryAdapter relationshipRepositoryForClass = registryEntry
            .getRelationshipRepositoryForClass(relationshipFieldClass, parameterProvider);
        RegistryEntry relationshipFieldEntry = resourceRegistry.getEntry(relationshipFieldClass);
        BaseResponseContext target;
        if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
            @SuppressWarnings("unchecked")
            JsonApiResponse response = relationshipRepositoryForClass
                .findManyTargets(castedResourceId, elementName, queryParams);

            List<LinkageContainer> dataList = getLinkages(relationshipFieldClass, relationshipFieldEntry, response);
            response.setEntity(dataList);
            target = new CollectionResponseContext(null, jsonPath, queryParams);
        } else {
            @SuppressWarnings("unchecked")
            JsonApiResponse response = relationshipRepositoryForClass
                .findOneTarget(castedResourceId, elementName, queryParams);

            if (response.getEntity() != null) {
                LinkageContainer linkageContainer = getLinkage(relationshipFieldClass, relationshipFieldEntry, response);
                response.setEntity(linkageContainer);
                target = new ResourceResponseContext(null, jsonPath, queryParams);
            } else {
                target = new ResourceResponseContext(null, jsonPath, queryParams);
            }
        }

        return target;
    }
}
