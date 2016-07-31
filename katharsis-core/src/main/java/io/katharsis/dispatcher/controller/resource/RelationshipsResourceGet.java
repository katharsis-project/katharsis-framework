package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.dispatcher.controller.Utils;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.Request;
import io.katharsis.request.path.JsonApiPath;
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

public class RelationshipsResourceGet extends ResourceIncludeField {

    public RelationshipsResourceGet(ResourceRegistry resourceRegistry,
                                    TypeParser typeParser,
                                    IncludeLookupSetter fieldSetter,
                                    QueryParamsBuilder paramsBuilder,
                                    ObjectMapper objectMapper) {
        super(resourceRegistry, typeParser, fieldSetter, paramsBuilder, objectMapper);
    }

    @Override
    public boolean isAcceptable(Request request) {
        return request.getMethod() == HttpMethod.GET && request.getPath().isRelationshipResource();
    }

    @Override
    public BaseResponseContext handle(Request request) {
        JsonApiPath path = request.getPath();

        RegistryEntry<?> registryEntry = resourceRegistry.getEntry(path.getResource());

        String elementName = path.getRelationship().get();

        ResourceField relationshipField = registryEntry.getResourceInformation()
                .findRelationshipFieldByName(elementName);

        Utils.checkResourceFieldExists(relationshipField, path.getResource());

        Class<?> baseRelationshipFieldClass = relationshipField.getType();
        Class<?> relationshipFieldClass = Generics
                .getResourceClass(relationshipField.getGenericType(), baseRelationshipFieldClass);

        RelationshipRepositoryAdapter relationshipRepositoryForClass = registryEntry
                .getRelationshipRepositoryForClass(relationshipFieldClass, request.getParameterProvider());

        RegistryEntry relationshipFieldEntry = resourceRegistry.getEntry(relationshipFieldClass);

        Serializable castedResourceId = parseResourceId(registryEntry, path.getIds().get());

        QueryParams queryParams = getQueryParamsBuilder().parseQuery(path.getQuery());

        BaseResponseContext target;

        if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
            @SuppressWarnings("unchecked")
            JsonApiResponse response = relationshipRepositoryForClass
                    .findManyTargets(castedResourceId, elementName, queryParams);

            includeFieldSetter.injectIncludedElementsForCollection(response, request, queryParams);

            List<LinkageContainer> dataList = getLinkages(relationshipFieldClass, relationshipFieldEntry, response);
            response.setEntity(dataList);
            target = new CollectionResponseContext(response, path, queryParams);
        } else {
            @SuppressWarnings("unchecked")
            JsonApiResponse response = relationshipRepositoryForClass
                    .findOneTarget(castedResourceId, elementName, queryParams);
            includeFieldSetter.injectIncludedRelationshipsInResource(response, request, queryParams);

            if (response.getEntity() != null) {
                LinkageContainer linkageContainer = getLinkage(relationshipFieldClass, relationshipFieldEntry, response);
                response.setEntity(linkageContainer);
                target = new ResourceResponseContext(response, path, queryParams);
            } else {
                target = new ResourceResponseContext(response, path, queryParams);
            }
        }

        return target;
    }

    private LinkageContainer getLinkage(Class<?> relationshipFieldClass, RegistryEntry relationshipFieldEntry, Object targetObject) {
        if (targetObject instanceof JsonApiResponse) {
            return new LinkageContainer(((JsonApiResponse) targetObject).getEntity(), relationshipFieldClass, relationshipFieldEntry);
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

    private Serializable parseResourceId(RegistryEntry<?> registryEntry, List<String> resourceIds) {
        String resourceId = resourceIds.get(0);
        return parseId(registryEntry, resourceId);
    }

}
