package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.FieldPath;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathIds;
import io.katharsis.resource.exception.RequestBodyException;
import io.katharsis.resource.exception.RequestBodyNotFoundException;
import io.katharsis.resource.exception.ResourceFieldNotFoundException;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.ResourceResponse;
import io.katharsis.utils.Generics;
import io.katharsis.utils.parser.TypeParser;
import org.apache.commons.beanutils.PropertyUtils;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

/**
 * Creates a new post in a similar manner as in {@link ResourcePost}, but additionally adds a relation to a field.
 */
public class FieldResourcePost extends ResourceUpsert {

    public FieldResourcePost(ResourceRegistry resourceRegistry, TypeParser typeParser, ObjectMapper objectMapper) {
        super(resourceRegistry, typeParser, objectMapper);
    }

    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection()
                && jsonPath instanceof FieldPath
                && HttpMethod.POST.name().equals(requestType);
    }

    @Override
    public ResourceResponse handle(JsonPath jsonPath, RequestParams requestParams, RequestBody requestBody)
        throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException,
        IOException {
        String resourceName = jsonPath.getResourceName();
        PathIds resourceIds = jsonPath.getIds();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);

        if (registryEntry == null) {
            throw new ResourceNotFoundException(resourceName);
        }
        if (requestBody == null) {
            throw new RequestBodyNotFoundException(HttpMethod.POST, resourceName);
        }
        if (requestBody.isMultiple()) {
            throw new RequestBodyException(HttpMethod.POST, resourceName, "Multiple data in body");
        }

        Serializable castedResourceId = getResourceId(resourceIds, registryEntry);
        Field relationshipField = registryEntry.getResourceInformation().findRelationshipFieldByName(jsonPath.getElementName());
        if (relationshipField == null) {
            throw new ResourceFieldNotFoundException(jsonPath.getElementName());
        }

        Class<?> baseRelationshipFieldClass = relationshipField.getType();
        Class<?> relationshipFieldClass = Generics.getResourceClass(relationshipField, baseRelationshipFieldClass);

        RegistryEntry relationshipRegistryEntry = resourceRegistry.getEntry(relationshipFieldClass);
        String relationshipResourceType = resourceRegistry.getResourceType(baseRelationshipFieldClass);

        DataBody dataBody = requestBody.getSingleData();
        Object resource = buildNewResource(relationshipRegistryEntry, dataBody, relationshipResourceType);
        setAttributes(dataBody, resource, relationshipRegistryEntry.getResourceInformation());
        Object savedResource = relationshipRegistryEntry.getResourceRepository().save(resource);
        saveRelations(savedResource, relationshipRegistryEntry, dataBody);

        String relationshipResourceIdName = relationshipRegistryEntry.getResourceInformation().getIdField().getName();
        Serializable resourceId = (Serializable) PropertyUtils.getProperty(savedResource, relationshipResourceIdName);

        Object savedResourceWithRelations = relationshipRegistryEntry.getResourceRepository().findOne(resourceId, requestParams);

        RelationshipRepository relationshipRepositoryForClass = registryEntry.getRelationshipRepositoryForClass(relationshipFieldClass);
        Object parent = registryEntry.getResourceRepository().findOne(castedResourceId, requestParams);
        if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
            relationshipRepositoryForClass.addRelations(parent, Collections.singletonList(resourceId), jsonPath.getElementName());
        } else {
            relationshipRepositoryForClass.setRelation(parent, resourceId, jsonPath.getElementName());
        }


        return new ResourceResponse(savedResourceWithRelations, jsonPath, requestParams);
    }

    private Serializable getResourceId(PathIds resourceIds, RegistryEntry<?> registryEntry) {
        String resourceId = resourceIds.getIds().get(0);
        Class<? extends Serializable> idClass = (Class<? extends Serializable>) registryEntry
                .getResourceInformation()
                .getIdField()
                .getType();
        return typeParser.parse(resourceId, idClass);
    }
}
