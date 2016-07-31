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
import io.katharsis.response.ResourceResponseContext;
import io.katharsis.utils.Generics;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;
import java.util.List;

public class FieldResourceGet extends ResourceIncludeField {

    public FieldResourceGet(ResourceRegistry resourceRegistry,
                            TypeParser typeParser,
                            IncludeLookupSetter fieldSetter,
                            QueryParamsBuilder paramsBuilder,
                            ObjectMapper objectMapper) {
        super(resourceRegistry, typeParser, fieldSetter, paramsBuilder, objectMapper);
    }

    @Override
    public boolean isAcceptable(Request request) {
        return request.getMethod() == HttpMethod.GET && request.getPath().isField();
    }

    @Override
    public BaseResponseContext handle(Request request) {
        JsonApiPath jsonPath = request.getPath();

        RegistryEntry<?> registryEntry = resourceRegistry.getEntry(jsonPath.getResource());
        Serializable castedResourceId = getResourceId(registryEntry, jsonPath.getIds().get());

        String elementName = jsonPath.getField().get();
        ResourceField relationshipField = registryEntry.getResourceInformation()
                .findRelationshipFieldByName(elementName);

        Utils.checkResourceFieldExists(relationshipField, elementName);

        Class<?> baseRelationshipFieldClass = relationshipField.getType();
        Class<?> relationshipFieldClass = Generics.getResourceClass(relationshipField.getGenericType(), baseRelationshipFieldClass);

        RelationshipRepositoryAdapter relationshipRepositoryForClass = registryEntry
                .getRelationshipRepositoryForClass(relationshipFieldClass, request.getParameterProvider());

        QueryParams queryParams = getQueryParamsBuilder().parseQuery(request.getQuery());

        BaseResponseContext target;

        if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
            @SuppressWarnings("unchecked")
            JsonApiResponse response = relationshipRepositoryForClass
                    .findManyTargets(castedResourceId, elementName, queryParams);

            includeFieldSetter.injectIncludedElementsForCollection(response, request, queryParams);
            target = new CollectionResponseContext(response, jsonPath, queryParams);
        } else {
            @SuppressWarnings("unchecked")
            JsonApiResponse response = relationshipRepositoryForClass
                    .findOneTarget(castedResourceId, elementName, queryParams);
            includeFieldSetter.injectIncludedRelationshipsInResource(response, request, queryParams);
            target = new ResourceResponseContext(response, jsonPath, queryParams);
        }

        return target;
    }

    private Serializable getResourceId(RegistryEntry<?> registryEntry, List<String> ids) {
        String resourceId = ids.get(0);
        return parseId(registryEntry, resourceId);
    }

}
