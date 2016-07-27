package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.HttpMethod;
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
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.CollectionResponseContext;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.LinkageContainer;
import io.katharsis.response.ResourceResponseContext;
import io.katharsis.utils.Generics;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RelationshipsResourceGet extends ResourceIncludeField  {

    public RelationshipsResourceGet(ResourceRegistry resourceRegistry, TypeParser typeParser, IncludeLookupSetter fieldSetter) {
        super(resourceRegistry, typeParser, fieldSetter);
    }

    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection()
            && jsonPath instanceof RelationshipsPath
            && HttpMethod.GET.name().equals(requestType);
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
            includeFieldSetter.setIncludedElements(resourceName, response, queryParams, parameterProvider);

            List<LinkageContainer> dataList = getLinkages(relationshipFieldClass, relationshipFieldEntry, response);
            response.setEntity(dataList);
            target = new CollectionResponseContext(response, jsonPath, queryParams);
        } else {
            @SuppressWarnings("unchecked")
            JsonApiResponse response = relationshipRepositoryForClass
                .findOneTarget(castedResourceId, elementName, queryParams);
            includeFieldSetter.setIncludedElements(resourceName, response, queryParams, parameterProvider);

            if (response.getEntity() != null) {
                LinkageContainer linkageContainer = getLinkage(relationshipFieldClass, relationshipFieldEntry, response);
                response.setEntity(linkageContainer);
                target = new ResourceResponseContext(response, jsonPath, queryParams);
            } else {
                target = new ResourceResponseContext(response, jsonPath, queryParams);
            }
        }

        return target;
    }

    private LinkageContainer getLinkage(Class<?> relationshipFieldClass, RegistryEntry relationshipFieldEntry, Object targetObject) {
        if (targetObject instanceof JsonApiResponse) {
            return new LinkageContainer(((JsonApiResponse)targetObject).getEntity(), relationshipFieldClass, relationshipFieldEntry);
        } else {
            return new LinkageContainer(targetObject, relationshipFieldClass, relationshipFieldEntry);
        }
    }

    private List<LinkageContainer> getLinkages(Class<?> relationshipFieldClass, RegistryEntry relationshipFieldEntry,
                                               JsonApiResponse targetObjects) {
        List<LinkageContainer> dataList = new ArrayList<>();
        if (targetObjects == null) {
            return dataList;
        }
        Iterable resources = (Iterable) targetObjects.getEntity();

        for (Object resource : resources) {
            dataList.add(new LinkageContainer(resource, relationshipFieldClass, relationshipFieldEntry));
        }
        return dataList;
    }

    private Serializable getResourceId(PathIds resourceIds, RegistryEntry<?> registryEntry) {
        String resourceId = resourceIds.getIds().get(0);
        @SuppressWarnings("unchecked") Class<? extends Serializable> idClass = (Class<? extends Serializable>) registryEntry
            .getResourceInformation()
            .getIdField()
            .getType();
        return typeParser.parse(resourceId, idClass);
    }
}
