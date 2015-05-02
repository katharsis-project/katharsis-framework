package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.FieldPath;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathIds;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.resource.exception.ResourceFieldNotFoundException;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.CollectionResponse;
import io.katharsis.response.ResourceResponse;
import io.katharsis.utils.Generics;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Set;

public class FieldResourceGet implements BaseController {

    private ResourceRegistry resourceRegistry;

    public FieldResourceGet(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection()
                && jsonPath instanceof FieldPath
                && "GET".equals(requestType);
    }

    @Override
    public BaseResponse handle(JsonPath jsonPath, RequestParams requestParams, RequestBody requestBody) {
        String resourceName = jsonPath.getResourceName();
        PathIds resourceIds = jsonPath.getIds();
        String resourceId = resourceIds.getIds().get(0);
        RegistryEntry<?> registryEntry = resourceRegistry.getEntry(resourceName);
        Set<Field> relationshipFields = registryEntry.getResourceInformation().getRelationshipFields();

        Class<?> baseRelationshipFieldClass = null;
        Class<?> relationshipFieldClass = null;
        for (Field relationshipField : relationshipFields) {
            if (relationshipField.getName().equals(jsonPath.getElementName())) {
                baseRelationshipFieldClass = relationshipField.getType();
                relationshipFieldClass = Generics.getResourceClass(relationshipField, baseRelationshipFieldClass);
            }
        }
        if (relationshipFieldClass == null) {
            throw new ResourceFieldNotFoundException("Resource field not found: " + jsonPath.getElementName());
        }
        RelationshipRepository relationshipRepositoryForClass = registryEntry.getRelationshipRepositoryForClass(relationshipFieldClass);
        BaseResponse target;
        if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
            Iterable targetObjects = relationshipRepositoryForClass.findTargets(castIdValue(resourceId, Long.class), jsonPath.getElementName());
            target = new CollectionResponse(targetObjects);
        } else {
            Object targetObject = relationshipRepositoryForClass.findOneTarget(castIdValue(resourceId, Long.class), jsonPath.getElementName());
            target = new ResourceResponse(targetObject);
        }

        return target;
    }

    // @TODO add more customized casting of ids
    private Serializable castIdValue(String id, Class<?> idType) {
        if (Long.class == idType) {
            return Long.valueOf(id);
        }
        return id;
    }
}
