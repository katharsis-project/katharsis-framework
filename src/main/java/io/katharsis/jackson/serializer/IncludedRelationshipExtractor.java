package io.katharsis.jackson.serializer;

import io.katharsis.jackson.exception.JsonSerializationException;
import io.katharsis.queryParams.include.Inclusion;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.annotations.JsonApiIncludeByDefault;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.Container;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Extracts inclusions from a resource.
 */
public class IncludedRelationshipExtractor {

    public Set<?> extractIncludedResources(Object resource, Set<Field> relationshipFields, BaseResponse response) throws JsonSerializationException {
        Set includedResources = new HashSet<>();
        includedResources.addAll(extractDefaultIncludedFields(resource, relationshipFields));
        try {
            includedResources.addAll(extractIncludedRelationships(resource, response));
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            // TODO log suppressed exception
        }

        return includedResources;
    }

    private List<?> extractDefaultIncludedFields(Object resource, Set<Field> relationshipFields) throws JsonSerializationException {
        List<?> includedResources = new LinkedList<>();
        for (Field relationshipField : relationshipFields) {
            if (relationshipField.isAnnotationPresent(JsonApiIncludeByDefault.class)) {
                includedResources.addAll(getIncludedFromRelation(relationshipField, resource));
            }
        }

        return includedResources;
    }

    private List<?> extractIncludedRelationships(Object resource, BaseResponse response)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
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
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
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
        return getElements(resource, pathList);
//        if (Iterable.class.isAssignableFrom(property.getClass())) {
//            Set includedResources = new HashSet<>();
//            ((Iterable)property).forEach(propertyItem -> includedResources.add(new Container<>(propertyItem)));
//            return includedResources;
//        } else {
//            return Collections.singleton(new Container<>(property));
//        }
    }

    private Set getElements(Object resource, List<String> pathList) {
        Set elements = new HashSet();
//        if (pathList.isEmpty()) {
//            if (resource != null) {
//                return Collections.singleton(resource);
//            } else {
//                return Collections.emptySet();
//            }
//        }
//        for (String propertyName : pathList) {
//            property = PropertyUtils.getProperty(property, propertyName);
//            if (property == null) {
//                return Collections.EMPTY_SET;
//            }
//            if (Iterable.class.isAssignableFrom(property.getClass())) {
//                ((Iterable)property).forEach( propertyItem -> {
//                    elements.addAll(getElements(propertyItem, pathList.subList(1, pathList.size())));
//                });
//            }
//
//        }
        return elements;
    }

    private List getIncludedFromRelation(Field relationshipField, Object resource) throws JsonSerializationException {
        List<Container> includedFields = new LinkedList<>();
        try {
            Object targetDataObj = PropertyUtils.getProperty(resource, relationshipField.getName());
            if (targetDataObj != null) {
                if (Iterable.class.isAssignableFrom(targetDataObj.getClass())) {
                    for (Object objectItem : (Iterable) targetDataObj) {
                        includedFields.add(new Container(objectItem));
                    }
                } else {
                    includedFields.add(new Container(targetDataObj));
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new JsonSerializationException("Exception while writing id field", e);
        }
        return includedFields;
    }
}
