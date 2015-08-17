package io.katharsis.jackson.serializer;

import io.katharsis.queryParams.include.Inclusion;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.ResourceField;
import io.katharsis.resource.annotations.JsonApiIncludeByDefault;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.Container;
import io.katharsis.utils.PropertyUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Extracts inclusions from a resource.
 */
public class IncludedRelationshipExtractor {

    public Set<?> extractIncludedResources(Object resource, Set<ResourceField> relationshipFields,
        BaseResponse response) {
        Set includedResources = new HashSet<>();
        includedResources.addAll(extractDefaultIncludedFields(resource, relationshipFields, response));
        try {
            includedResources.addAll(extractIncludedRelationships(resource, response));
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | NoSuchFieldException e) {
            // TODO log suppressed exception
        }

        return includedResources;
    }

    private List<?> extractDefaultIncludedFields(Object resource, Set<ResourceField> relationshipFields,
        BaseResponse response) {
        List<?> includedResources = new LinkedList<>();
        for (ResourceField relationshipField : relationshipFields) {
            if (relationshipField.isAnnotationPresent(JsonApiIncludeByDefault.class)) {
                includedResources.addAll(getIncludedFromRelation(relationshipField, resource, response));
            }
        }

        return includedResources;
    }

    private List<?> extractIncludedRelationships(Object resource, BaseResponse response)
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
        List<?> includedResources = new LinkedList<>();
        List<Inclusion> includedRelations = response.getRequestParams().getIncludedRelations();
        if (includedRelations != null) {
            for (Inclusion inclusion : includedRelations) {
                includedResources.addAll(extractIncludedRelationship(resource, inclusion, response));
            }
        }
        return includedResources;
    }

    private Set extractIncludedRelationship(Object resource, Inclusion inclusion, BaseResponse response)
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
        List<String> pathList = inclusion.getPathList();
        if (resource == null || pathList.isEmpty()) {
            return Collections.EMPTY_SET;
        }
        if (!(response.getJsonPath() instanceof ResourcePath)) { // the first property name is the resource itself
            pathList = pathList.subList(1, pathList.size());
            if (pathList.isEmpty()) {
                return Collections.EMPTY_SET;
            }
        }
        return getElements(resource, pathList, response);
    }

    private Set getElements(Object resource, List<String> pathList, BaseResponse response)
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
        Set elements = new HashSet();
        if (pathList.isEmpty()) {
            if (resource != null) {
                return Collections.singleton(new Container(resource, response.getRequestParams()));
            } else {
                return Collections.emptySet();
            }
        }
        Field field = resource.getClass().getDeclaredField(pathList.get(0));
        Object property = PropertyUtils.getProperty(resource, field.getName());
        if (property != null) {
            List<String> subPathList = pathList.subList(1, pathList.size());
            if (Iterable.class.isAssignableFrom(property.getClass())) {
                Iterator iterator = ((Iterable) property).iterator();
                while (iterator.hasNext()) {
                    elements.addAll(getElements(iterator.next(), subPathList, response));
                }
            } else {
                elements.addAll(getElements(property, subPathList, response));
            }
        } else {
            return Collections.emptySet();
        }
        return elements;
    }

    private List getIncludedFromRelation(ResourceField relationshipField, Object resource, BaseResponse response) {
        List<Container> includedFields = new LinkedList<>();
        Object targetDataObj = PropertyUtils.getProperty(resource, relationshipField.getName());
        if (targetDataObj != null) {
            if (Iterable.class.isAssignableFrom(targetDataObj.getClass())) {
                for (Object objectItem : (Iterable) targetDataObj) {
                    includedFields.add(new Container(objectItem, response.getRequestParams()));
                }
            } else {
                includedFields.add(new Container(targetDataObj, response.getRequestParams()));
            }
        }
        return includedFields;
    }
}
