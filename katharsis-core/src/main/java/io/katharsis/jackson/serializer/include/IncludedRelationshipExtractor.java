package io.katharsis.jackson.serializer.include;

import io.katharsis.queryParams.include.Inclusion;
import io.katharsis.queryParams.params.IncludedRelationsParams;
import io.katharsis.queryParams.params.TypedParams;
import io.katharsis.request.path.FieldPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.annotations.JsonApiIncludeByDefault;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.exception.ResourceFieldNotFoundException;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.Container;
import io.katharsis.response.ContainerType;
import io.katharsis.utils.ClassUtils;
import io.katharsis.utils.PropertyUtils;
import io.katharsis.utils.java.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Extracts inclusions from a resource.
 */
public class IncludedRelationshipExtractor {
    private static final Logger logger = LoggerFactory.getLogger(IncludedRelationshipExtractor.class);
    private final ResourceRegistry resourceRegistry;

    public IncludedRelationshipExtractor(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    public Map<ResourceDigest, Container> extractIncludedResources(Object resource, BaseResponseContext response) {
        Map<ResourceDigest, Container> includedResources = new HashMap<>();

        populateIncludedByDefaultResources(resource, response, ContainerType.TOP, includedResources, 1);
        try {
            populateIncludedRelationships(resource, response, includedResources);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | NoSuchFieldException e) {
            logger.info("Exception while extracting included fields", e);
        }
        return includedResources;
    }


    private void populateIncludedByDefaultResources(Object resource,
                                                    BaseResponseContext response,
                                                    ContainerType containerType,
                                                    Map<ResourceDigest, Container> includedResources,
                                                    int recurrenceLevel) {
        int recurrenceLevelCounter = recurrenceLevel;
        if (recurrenceLevel >= 42 || resource == null) {
            return;
        }

        Set<ResourceField> relationshipFields = getRelationshipFields(resource);

        //noinspection unchecked
        for (ResourceField resourceField : relationshipFields) {
            Object targetDataObj = PropertyUtils.getProperty(resource, resourceField.getUnderlyingName());
            if (targetDataObj == null) {
                continue;
            }
            if (resourceField.isAnnotationPresent(JsonApiIncludeByDefault.class)) {
                recurrenceLevelCounter++;
                if (targetDataObj instanceof Iterable) {
                    for (Object objectItem : (Iterable) targetDataObj) {
                        includedResources.put(getResourceDigest(objectItem), new Container(objectItem, response, containerType));
                        populateIncludedByDefaultResourcesRecursive(objectItem, response, containerType, includedResources, recurrenceLevelCounter);
                    }
                } else {
                    includedResources.put(getResourceDigest(targetDataObj), new Container(targetDataObj, response, containerType));
                    populateIncludedByDefaultResourcesRecursive(targetDataObj, response, containerType, includedResources, recurrenceLevelCounter);
                }
            } // if this is a top level container and its field matches the included parameters traverse further to find defaults
            else if (containerType.equals(ContainerType.TOP) && isFieldIncluded(response, resourceField.getUnderlyingName())) {
                if (targetDataObj instanceof Iterable) {
                    for (Object objectItem : (Iterable) targetDataObj) {
                        populateIncludedByDefaultResourcesRecursive(objectItem,
                                response,
                                containerType,
                                includedResources,
                                recurrenceLevelCounter);
                    }
                } else {
                    populateIncludedByDefaultResourcesRecursive(targetDataObj,
                            response,
                            containerType,
                            includedResources,
                            recurrenceLevelCounter);
                }
            }
        }
    }

    private boolean isFieldIncluded(BaseResponseContext response, String fieldName) {
        if (response.getQueryParams() == null ||
                response.getQueryParams().getIncludedRelations() == null ||
                response.getQueryParams().getIncludedRelations().getParams() == null) {
            return false;
        }
        IncludedRelationsParams includedRelationsParams = response.getQueryParams().getIncludedRelations().getParams().get(response.getJsonPath().getElementName());
        if (includedRelationsParams == null ||
                includedRelationsParams.getParams() == null) {
            return false;
        }

        for (Inclusion inclusion : includedRelationsParams.getParams()) {
            if (inclusion.getPathList().get(0).equals(fieldName)) {
                return true;
            }
        }

        return false;

    }

    private void populateIncludedByDefaultResourcesRecursive(Object targetDataObj,
                                                             BaseResponseContext response,
                                                             ContainerType containerType,
                                                             Map<ResourceDigest, Container> includedResourceContainers,
                                                             int recurrenceLevelCounter) {
        if (containerType.equals(ContainerType.TOP)) {
            populateIncludedByDefaultResources(targetDataObj, response, ContainerType.INCLUDED_DEFAULT, includedResourceContainers, recurrenceLevelCounter);
        } else if (containerType.equals(ContainerType.INCLUDED_DEFAULT)) {
            populateIncludedByDefaultResources(targetDataObj, response, ContainerType.INCLUDED_DEFAULT_NESTED, includedResourceContainers, recurrenceLevelCounter);
        } else {
            populateIncludedByDefaultResources(targetDataObj, response, ContainerType.INCLUDED_DEFAULT_NESTED, includedResourceContainers, recurrenceLevelCounter);
        }
    }

    private void populateIncludedRelationships(Object resource, BaseResponseContext response, Map<ResourceDigest, Container> includedResources)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
        if (response.getQueryParams() == null || response.getJsonPath() == null) {
            return;
        }
        TypedParams<IncludedRelationsParams> includedRelations = response.getQueryParams()
                .getIncludedRelations();
        String elementName = response.getJsonPath()
                .getElementName();

        // handle field paths differently because the element name is not its type but field name (#357)
        if (response.getJsonPath() instanceof FieldPath) {
            // extract the resource's resource type name
            Optional<JsonApiResource> optional = ClassUtils.getAnnotation(resource.getClass(), JsonApiResource.class);
            if (optional.isPresent()) {
                elementName = optional.get().type();
            }
        }
        IncludedRelationsParams includedRelationsParams = findInclusions(includedRelations, elementName);
        if (includedRelationsParams != null) {
            for (Inclusion inclusion : includedRelationsParams.getParams()) {
                //noinspection unchecked
                populateIncludedRelationship(resource, inclusion, response, includedResources);
            }
        }
    }

    private IncludedRelationsParams findInclusions(TypedParams<IncludedRelationsParams> queryParams,
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

    private void populateIncludedRelationship(Object resource, Inclusion inclusion, BaseResponseContext response, Map<ResourceDigest, Container> includedResources)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
        List<String> pathList = inclusion.getPathList();
        if (resource == null || pathList.isEmpty()) {
            return;
        }
        if (!(response.getJsonPath() instanceof ResourcePath) && !(response.getJsonPath() instanceof FieldPath)) {
            // the first property name is the resource itself
            pathList = pathList.subList(1, pathList.size());
            if (pathList.isEmpty()) {
                return;
            }
        }
        populateIncludedResources(resource, pathList, response, includedResources);
    }

    private void populateIncludedResources(Object resource, List<String> pathList, BaseResponseContext response, Map<ResourceDigest, Container> includedResources)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {


        String fieldName = getRelationshipName(pathList.get(0), resource.getClass());
        Object resourceProperty = PropertyUtils.getProperty(resource, fieldName);
        if (resourceProperty != null) {
            populateToResourcePropertyToIncludedResources(resourceProperty, response, ContainerType.INCLUDED, pathList.get(0), includedResources);
            if (pathList.size() > 1) {
                if (Iterable.class.isAssignableFrom(resourceProperty.getClass())
                        && ((Iterable) resourceProperty).iterator().hasNext()) {
                    resourceProperty = ((Iterable) resourceProperty).iterator().next();
                    fieldName = getRelationshipName(pathList.get(1), resourceProperty.getClass());
                    resourceProperty = PropertyUtils.getProperty(resourceProperty, fieldName);
                    populateToResourcePropertyToIncludedResources(resourceProperty, response, ContainerType.INCLUDED_NESTED, pathList.get(1), includedResources);
                } else {
                    fieldName = getRelationshipName(pathList.get(1), resourceProperty.getClass());
                    resourceProperty = PropertyUtils.getProperty(resourceProperty, fieldName);
                    populateToResourcePropertyToIncludedResources(resourceProperty, response, ContainerType.INCLUDED_NESTED, pathList.get(1), includedResources);
                }

            }
        }

    }

    private void populateToResourcePropertyToIncludedResources(Object resourceProperty, BaseResponseContext response, ContainerType containerType, String includedFieldName, Map<ResourceDigest, Container> includedResources) {
        if (resourceProperty != null) {
            if (Iterable.class.isAssignableFrom(resourceProperty.getClass())) {
                for (Object resourceToInclude : (Iterable) resourceProperty) {
                    ResourceDigest digest = getResourceDigest(resourceToInclude);
                    includedResources.put(digest, new Container(resourceToInclude, response, containerType, includedFieldName));
                }
            } else {
                ResourceDigest digest = getResourceDigest(resourceProperty);
                includedResources.put(digest, new Container(resourceProperty, response, containerType, includedFieldName));
            }
        }
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

    private ResourceDigest getResourceDigest(Object resource) {
        Class<?> resourceClass = ClassUtils.getJsonApiResourceClass(resource);
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceClass);
        String idFieldName = registryEntry.getResourceInformation().getIdField().getUnderlyingName();
        Object idValue = PropertyUtils.getProperty(resource, idFieldName);
        String resourceType = resourceRegistry.getResourceType(resourceClass);
        return new ResourceDigest(idValue, resourceType);
    }
}
