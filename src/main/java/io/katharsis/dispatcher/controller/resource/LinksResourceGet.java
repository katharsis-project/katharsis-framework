package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.LinksPath;
import io.katharsis.request.path.PathIds;
import io.katharsis.resource.exception.ResourceFieldNotFoundException;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.CollectionResponse;
import io.katharsis.response.LinkageContainer;
import io.katharsis.response.ResourceResponse;
import io.katharsis.utils.Generics;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class LinksResourceGet implements BaseController {

    private ResourceRegistry resourceRegistry;
    private TypeParser typeParser;

    public LinksResourceGet(ResourceRegistry resourceRegistry, TypeParser typeParser) {
        this.resourceRegistry = resourceRegistry;
        this.typeParser = typeParser;
    }

    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection()
                && jsonPath instanceof LinksPath
                && HttpMethod.GET.name().equals(requestType);
    }

    @Override
    public BaseResponse handle(JsonPath jsonPath, RequestParams requestParams, RequestBody requestBody)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String resourceName = jsonPath.getResourceName();
        PathIds resourceIds = jsonPath.getIds();
        RegistryEntry<?> registryEntry = resourceRegistry.getEntry(resourceName);

        Serializable castedResourceId = getResourceId(resourceIds, registryEntry);
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
            throw new ResourceFieldNotFoundException(jsonPath.getElementName());
        }
        RelationshipRepository relationshipRepositoryForClass = registryEntry.getRelationshipRepositoryForClass(relationshipFieldClass);
        RegistryEntry relationshipFieldEntry = resourceRegistry.getEntry(relationshipFieldClass);
        BaseResponse target;
        if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
            List<LinkageContainer> dataList = new LinkedList<>();

            Iterable targetObjects = relationshipRepositoryForClass.findTargets(castedResourceId, jsonPath.getElementName());
            if (targetObjects != null) {
                for (Object targetObject : targetObjects) {
                    dataList.add(new LinkageContainer(targetObject, relationshipFieldClass, relationshipFieldEntry));
                }
            }
            target = new CollectionResponse(dataList);
        } else {
            Object targetObject = relationshipRepositoryForClass.findOneTarget(castedResourceId, jsonPath.getElementName());
            if (targetObject != null) {
                LinkageContainer linkageContainer = new LinkageContainer(targetObject, relationshipFieldClass, relationshipFieldEntry);
                target = new ResourceResponse(linkageContainer);
            } else {
                target = new ResourceResponse(null);
            }
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
