package io.katharsis.jackson.serializer;

import io.katharsis.queryParams.include.Inclusion;
import io.katharsis.queryParams.params.IncludedRelationsParams;
import io.katharsis.queryParams.params.TypedParams;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.annotations.JsonApiIncludeByDefault;
import io.katharsis.resource.exception.ResourceFieldNotFoundException;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.Container;
import io.katharsis.utils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Extracts inclusions from a resource.
 */
public class IncludedRelationshipExtractor {
    private static final Logger logger = LoggerFactory.getLogger(IncludedRelationshipExtractor.class);
    private final ResourceRegistry resourceRegistry;

    public IncludedRelationshipExtractor(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    public Set<?> extractIncludedResources(Object resource, BaseResponseContext response) {
        Set includedResources = new HashSet<>();
        //noinspection unchecked
        includedResources.addAll(extractDefaultIncludedFields(resource, response));
        try {
            //noinspection unchecked
            includedResources.addAll(extractIncludedRelationships(resource, response));
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | NoSuchFieldException e) {
            logger.info("Exception while extracting included fields", e);
        }

        return includedResources;
    }

    private List<?> extractDefaultIncludedFields(Object resource, BaseResponseContext response) {
        List<?> includedResources = getIncludedByDefaultResources(resource, 1);
        List<Container> includedResourceContainers = new ArrayList<>(includedResources.size());
        for (Object includedResource : includedResources) {
            includedResourceContainers.add(new Container(includedResource, response));
        }

        return includedResourceContainers;
    }


    private List<?> getIncludedByDefaultResources(Object resource, int recurrenceLevel) {
        int recurrenceLevelCounter = recurrenceLevel;
        if (recurrenceLevel >= 42 || resource == null) {
            return Collections.emptyList();
        }

        Set<ResourceField> relationshipFields = getRelationshipFields(resource);
        List includedFields = new LinkedList<>();

        //noinspection unchecked
        for (ResourceField resourceField : relationshipFields) {
            if (resourceField.isAnnotationPresent(JsonApiIncludeByDefault.class)) {

                Object targetDataObj = PropertyUtils.getProperty(resource, resourceField.getUnderlyingName());

                if (targetDataObj != null) {
                    recurrenceLevelCounter++;

                    if (targetDataObj instanceof Iterable) {
                        for (Object objectItem : (Iterable) targetDataObj) {
                            //noinspection unchecked
                            includedFields.add(objectItem);
                            //noinspection unchecked
                            includedFields.addAll(getIncludedByDefaultResources(objectItem, recurrenceLevelCounter));
                        }
                    } else {
                        //noinspection unchecked
                        includedFields.add(targetDataObj);
                        //noinspection unchecked
                        includedFields.addAll(getIncludedByDefaultResources(targetDataObj, recurrenceLevelCounter));
                    }
                }
            }
        }

        return includedFields;
    }

    private List<?> extractIncludedRelationships(Object resource, BaseResponseContext response)
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
        List<?> includedResources = new LinkedList<>();
        TypedParams<IncludedRelationsParams> includedRelations = response.getQueryParams()
            .getIncludedRelations();
        String elementName = response.getJsonPath()
            .getElementName();
        IncludedRelationsParams includedRelationsParams = findInclusions(includedRelations, elementName);
        if (includedRelationsParams != null) {
            for (Inclusion inclusion : includedRelationsParams.getParams()) {
                //noinspection unchecked
                includedResources.addAll(extractIncludedRelationship(resource, inclusion, response));
            }
        }
        return includedResources;
    }

    private static IncludedRelationsParams findInclusions(TypedParams<IncludedRelationsParams> queryParams,
                                                   String resourceName) {
        if (queryParams != null && queryParams.getParams() != null) {
            for (Map.Entry<String, IncludedRelationsParams> entry : queryParams.getParams()
                .entrySet()) {
                if (resourceName.equals(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private Set extractIncludedRelationship(Object resource, Inclusion inclusion, BaseResponseContext response)
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
        List<String> pathList = inclusion.getPathList();
        if (resource == null || pathList.isEmpty()) {
            return Collections.emptySet();
        }
        if (!(response.getJsonPath() instanceof ResourcePath)) { // the first property name is the resource itself
            pathList = pathList.subList(1, pathList.size());
            if (pathList.isEmpty()) {
                return Collections.emptySet();
            }
        }
        return getElements(resource, pathList, response);
    }

    private Set getElements(Object resource, List<String> pathList, BaseResponseContext response)
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
        Set elements = new HashSet();

        String fieldName = getRelationshipName(pathList.get(0), resource.getClass());

        Object property = PropertyUtils.getProperty(resource, fieldName);
        if (property != null) {
            if (Iterable.class.isAssignableFrom(property.getClass())) {
                for (Object o : (Iterable) property) {
                    //noinspection unchecked
                    elements.add(new Container(o, response));
                }
            } else {
                //noinspection unchecked
                elements.add(new Container(property, response));
            }
        } else {
            return Collections.emptySet();
        }
        return elements;
    }

    private <T> String getRelationshipName(String jsonName, Class<T> resourceClazz) {
        RegistryEntry resourceEntry = resourceRegistry.getEntry(resourceClazz);
        ResourceField relationshipField = resourceEntry.getResourceInformation().findRelationshipFieldByName(jsonName);
        if (relationshipField == null) {
            throw new ResourceFieldNotFoundException(String.format("%s for %s has been not found", jsonName, resourceClazz));
        }
        return relationshipField.getUnderlyingName();
    }

    private Set<ResourceField> getRelationshipFields(Object resource) {
        Class<?> dataClass = resource.getClass();
        RegistryEntry entry = resourceRegistry.getEntry(dataClass);
        ResourceInformation resourceInformation = entry.getResourceInformation();
        return resourceInformation.getRelationshipFields();
    }
}
