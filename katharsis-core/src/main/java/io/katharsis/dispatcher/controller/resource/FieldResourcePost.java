package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.dispatcher.controller.Utils;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.Request;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.RelationshipRepositoryAdapter;
import io.katharsis.resource.registry.responseRepository.ResourceRepositoryAdapter;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.HttpStatus;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.ResourceResponseContext;
import io.katharsis.utils.Generics;
import io.katharsis.utils.PropertyUtils;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Creates a new post in a similar manner as in {@link ResourcePost}, but additionally adds a relation to a field.
 */
public class FieldResourcePost extends ResourceUpsert {

    public FieldResourcePost(ResourceRegistry resourceRegistry,
                             TypeParser typeParser,
                             QueryParamsBuilder paramsBuilder,
                             ObjectMapper objectMapper) {
        super(resourceRegistry, typeParser, paramsBuilder, objectMapper);
    }

    @Override
    public boolean isAcceptable(Request request) {
        return request.getMethod() == HttpMethod.POST && request.getPath().isField();
    }

    @Override
    public BaseResponseContext handle(Request request) {
        JsonApiPath path = request.getPath();

        String elementName = path.getField().get();
        RegistryEntry endpointRegistryEntry = resourceRegistry.getEntry(path.getResource());
        Utils.checkResourceExists(endpointRegistryEntry, path.getResource());

//        if (requestBody == null) {
//            throw new RequestBodyNotFoundException(HttpMethod.POST, resourceEndpointName);
//        }
//        if (requestBody.isMultiple()) {
//            throw new RequestBodyException(HttpMethod.POST, resourceEndpointName, "Multiple data in body");
//        }

        Serializable castedResourceId = getResourceId(endpointRegistryEntry, request.getPath().getIds().get());

        ResourceField relationshipField = endpointRegistryEntry.getResourceInformation()
                .findRelationshipFieldByName(elementName);

        Utils.checkResourceFieldExists(relationshipField, elementName);

        Class<?> baseRelationshipFieldClass = relationshipField.getType();
        Class<?> relationshipFieldClass = Generics
                .getResourceClass(relationshipField.getGenericType(), baseRelationshipFieldClass);

        RegistryEntry relationshipRegistryEntry = resourceRegistry.getEntry(relationshipFieldClass);
        String relationshipResourceType = resourceRegistry.getResourceType(relationshipFieldClass);

        DataBody dataBody = dataBody(request);
        Object resource = buildNewResource(relationshipRegistryEntry, dataBody, relationshipResourceType);
        setAttributes(dataBody, resource, relationshipRegistryEntry.getResourceInformation());

        ResourceRepositoryAdapter resourceRepository = relationshipRegistryEntry.getResourceRepository(request.getParameterProvider());

        QueryParams queryParams = getQueryParamsBuilder().parseQuery(request.getQuery());

        JsonApiResponse savedResourceResponse = resourceRepository.save(resource, queryParams);
        saveRelations(queryParams, extractResource(savedResourceResponse), relationshipRegistryEntry, dataBody);

        Serializable resourceId = (Serializable) PropertyUtils
                .getProperty(extractResource(savedResourceResponse), relationshipRegistryEntry.getResourceInformation()
                        .getIdField()
                        .getUnderlyingName());

        RelationshipRepositoryAdapter relationshipRepositoryForClass = endpointRegistryEntry
                .getRelationshipRepositoryForClass(relationshipFieldClass, request.getParameterProvider());

        @SuppressWarnings("unchecked")
        JsonApiResponse parent = endpointRegistryEntry.getResourceRepository(request.getParameterProvider())
                .findOne(castedResourceId, queryParams);
        if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
            //noinspection unchecked
            relationshipRepositoryForClass.addRelations(parent.getEntity(), Collections.singletonList(resourceId),
                    elementName, queryParams);
        } else {
            //noinspection unchecked
            relationshipRepositoryForClass.setRelation(parent.getEntity(), resourceId, elementName, queryParams);
        }
        return new ResourceResponseContext(savedResourceResponse, path, queryParams, HttpStatus.CREATED_201);
    }

    private Serializable getResourceId(RegistryEntry<?> registryEntry, List<String> ids) {
        String resourceId = ids.get(0);
        return parseId(registryEntry, resourceId);
    }

}
