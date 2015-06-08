package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.FieldPath;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathIds;
import io.katharsis.resource.exception.ResourceFieldNotFoundException;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.CollectionResponse;
import io.katharsis.response.ResourceResponse;
import io.katharsis.utils.Generics;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class FieldResourceGet implements BaseController {

    private ResourceRegistry resourceRegistry;
    private TypeParser typeParser;

    public FieldResourceGet(ResourceRegistry resourceRegistry, TypeParser typeParser) {
        this.resourceRegistry = resourceRegistry;
        this.typeParser = typeParser;
    }

    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection()
                && jsonPath instanceof FieldPath
                && HttpMethod.GET.name().equals(requestType);
    }

    @Override
    public BaseResponse handle(JsonPath jsonPath, RequestParams requestParams, RequestBody requestBody)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String resourceName = jsonPath.getResourceName();
        PathIds resourceIds = jsonPath.getIds();

        RegistryEntry<?> registryEntry = resourceRegistry.getEntry(resourceName);
        Serializable castedResourceId = getResourceId(resourceIds, registryEntry);
        Field relationshipField = registryEntry.getResourceInformation().findRelationshipFieldByName(jsonPath.getElementName());
        if (relationshipField == null) {
            throw new ResourceFieldNotFoundException(jsonPath.getElementName());
        }

        Class<?> baseRelationshipFieldClass = relationshipField.getType();
        Class<?> relationshipFieldClass = Generics.getResourceClass(relationshipField, baseRelationshipFieldClass);

        RelationshipRepository relationshipRepositoryForClass = registryEntry.getRelationshipRepositoryForClass(relationshipFieldClass);
        BaseResponse target;
        if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
            Iterable targetObjects = relationshipRepositoryForClass.findManyTargets(castedResourceId, jsonPath.getElementName());
            target = new CollectionResponse(targetObjects);
        } else {
            Object targetObject = relationshipRepositoryForClass.findOneTarget(castedResourceId, jsonPath.getElementName());
            target = new ResourceResponse(targetObject);
        }

        return target;
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
