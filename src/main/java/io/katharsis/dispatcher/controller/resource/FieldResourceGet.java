package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.FieldPath;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathIds;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.exception.ResourceFieldNotFoundException;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.*;
import io.katharsis.utils.Generics;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

public class FieldResourceGet extends ResourceIncludeField {

    public FieldResourceGet(ResourceRegistry resourceRegistry, TypeParser typeParser, IncludeLookupSetter fieldSetter) {
        super(resourceRegistry,typeParser,fieldSetter);
    }

    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection()
                && jsonPath instanceof FieldPath
                && HttpMethod.GET.name().equals(requestType);
    }

    @Override
    public BaseResponse handle(JsonPath jsonPath, RequestParams requestParams, RepositoryMethodParameterProvider parameterProvider, RequestBody requestBody)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        String resourceName = jsonPath.getResourceName();
        PathIds resourceIds = jsonPath.getIds();

        RegistryEntry<?> registryEntry = resourceRegistry.getEntry(resourceName);
        Serializable castedResourceId = getResourceId(resourceIds, registryEntry);
        String elementName = jsonPath.getElementName();
        ResourceField relationshipField = registryEntry.getResourceInformation().findRelationshipFieldByName(elementName);
        if (relationshipField == null) {
            throw new ResourceFieldNotFoundException(elementName);
        }

        Class<?> baseRelationshipFieldClass = relationshipField.getType();
        Class<?> relationshipFieldClass = Generics.getResourceClass(relationshipField.getGenericType(), baseRelationshipFieldClass);

        RelationshipRepository relationshipRepositoryForClass = registryEntry.getRelationshipRepositoryForClass(relationshipFieldClass, parameterProvider);
        BaseResponse target;
        if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
            @SuppressWarnings("unchecked")
            Iterable<?> targetObjects = relationshipRepositoryForClass
                    .findManyTargets(castedResourceId, elementName, requestParams);
            includeFieldSetter.setIncludedElements(targetObjects, requestParams, parameterProvider);
            MetaInformation metaInformation = getMetaInformation(relationshipRepositoryForClass, targetObjects, requestParams);
            LinksInformation linksInformation = getLinksInformation(relationshipRepositoryForClass, targetObjects, requestParams);
            target = new CollectionResponse(targetObjects, jsonPath, requestParams, metaInformation, linksInformation);
        } else {
            @SuppressWarnings("unchecked")
            Object targetObject = relationshipRepositoryForClass.findOneTarget(castedResourceId, elementName, requestParams);
            includeFieldSetter.setIncludedElements(targetObject, requestParams, parameterProvider);
            MetaInformation metaInformation =
                    getMetaInformation(relationshipRepositoryForClass, Collections.singletonList(targetObject), requestParams);
            LinksInformation linksInformation =
                    getLinksInformation(relationshipRepositoryForClass, Collections.singletonList(targetObject), requestParams);
            target = new ResourceResponse(targetObject, jsonPath, requestParams, metaInformation, linksInformation);
        }

        return target;
    }

    private Serializable getResourceId(PathIds resourceIds, RegistryEntry<?> registryEntry) {
        String resourceId = resourceIds.getIds().get(0);
        @SuppressWarnings("unchecked")
        Class<? extends Serializable> idClass = (Class<? extends Serializable>) registryEntry
                .getResourceInformation()
                .getIdField()
                .getType();
        return typeParser.parse(resourceId, idClass);
    }
}
