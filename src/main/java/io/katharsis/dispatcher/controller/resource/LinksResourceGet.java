package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.path.JsonPath;
import io.katharsis.path.LinksPath;
import io.katharsis.path.PathBuilder;
import io.katharsis.path.PathIds;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.resource.exception.ResourceFieldNotFoundException;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.*;
import io.katharsis.utils.Generics;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class LinksResourceGet implements BaseController {

    private ResourceRegistry resourceRegistry;
    private PathBuilder pathBuilder;

    public LinksResourceGet(ResourceRegistry resourceRegistry, PathBuilder pathBuilder) {
        this.resourceRegistry = resourceRegistry;
        this.pathBuilder = pathBuilder;
    }

    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection()
                && jsonPath instanceof LinksPath
                && "GET".equals(requestType);
    }

    @Override
    public BaseResponse handle(JsonPath jsonPath, RequestParams requestParams) {
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
        RegistryEntry relationshipFieldEntry = resourceRegistry.getEntry(relationshipFieldClass);
        BaseResponse target;
        TopLevelLinks topLevelLinks = new TopLevelLinksLinks(pathBuilder.buildPath(jsonPath), pathBuilder.buildPath
                (jsonPath, true));
        if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
            List<LinkageContainer> dataList = new LinkedList<>();

            Iterable targetObjects = relationshipRepositoryForClass.findTargets(castIdValue(resourceId, Long.class), jsonPath.getElementName());
            if (targetObjects != null) {
                for (Object targetObject : targetObjects) {
                    dataList.add(new LinkageContainer(targetObject, relationshipFieldClass, relationshipFieldEntry));
                }
            }
            target = new CollectionResponse(dataList, topLevelLinks);
        } else {
            Object targetObject = relationshipRepositoryForClass.findOneTarget(castIdValue(resourceId, Long.class), jsonPath.getElementName());
            if (targetObject != null) {
                LinkageContainer linkageContainer = new LinkageContainer(targetObject, relationshipFieldClass, relationshipFieldEntry);
                target = new ResourceResponse(linkageContainer, topLevelLinks);
            } else {
                target = new ResourceResponse(null, topLevelLinks);
            }
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
