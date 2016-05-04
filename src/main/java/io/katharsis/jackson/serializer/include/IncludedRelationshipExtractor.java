package io.katharsis.jackson.serializer.include;

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
import io.katharsis.utils.ClassUtils;
import io.katharsis.utils.PropertyUtils;
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

    public  Map<ResourceDigest, Container> extractIncludedResources(Object resource, BaseResponseContext response) {
        Map<ResourceDigest, Container> includedResources = new HashMap<>();
        //noinspection unchecked
        includedResources.putAll(extractDefaultIncludedFields(resource, response));
        try {
            //noinspection unchecked
            includedResources.putAll(extractIncludedRelationships(resource, response));
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | NoSuchFieldException e) {
            logger.info("Exception while extracting included fields", e);
        }

        return includedResources;
    }

    private  Map<ResourceDigest, Container> extractDefaultIncludedFields(Object resource, BaseResponseContext response) {
        List<?> includedResources = getIncludedByDefaultResources(resource, 1);
        Map<ResourceDigest, Container> includedResourceContainers = new HashMap<>(includedResources.size());
        for (Object includedResource : includedResources) {
            ResourceDigest digest = getResourceDigest(includedResource);
            includedResourceContainers.put(digest, new Container(includedResource, response));
        }

        return includedResourceContainers;
    }


    private List<?> getIncludedByDefaultResources(Object resource, int recurrenceLevel) {
        int recurrenceLevelCounter = recurrenceLevel;
        if (recurrenceLevel >= 42 || resource == null) {
            return Collections.emptyList();
        }

        Set<ResourceField> relationshipFields = getRelationshipFields(resource);
        List includedFields = new ArrayList();

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

    private Map<ResourceDigest, Container> extractIncludedRelationships(Object resource, BaseResponseContext response)
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
        Map<ResourceDigest, Container> includedResources = new HashMap<>();
        TypedParams<IncludedRelationsParams> includedRelations = response.getQueryParams()
            .getIncludedRelations();
        String elementName = response.getJsonPath()
            .getElementName();
        IncludedRelationsParams includedRelationsParams = findInclusions(includedRelations, elementName);
        if (includedRelationsParams != null) {
            for (Inclusion inclusion : includedRelationsParams.getParams()) {
                //noinspection unchecked
                includedResources.putAll(extractIncludedRelationship(resource, inclusion, response));
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

    private Map<ResourceDigest, Container> extractIncludedRelationship(Object resource, Inclusion inclusion, BaseResponseContext response)
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
        List<String> pathList = inclusion.getPathList();
        if (resource == null || pathList.isEmpty()) {
            return Collections.emptyMap();
        }
        if (!(response.getJsonPath() instanceof ResourcePath)) { // the first property name is the resource itself
            pathList = pathList.subList(1, pathList.size());
            if (pathList.isEmpty()) {
                return Collections.emptyMap();
            }
        }
        return getElements(resource, pathList, response);
    }

    private Map<ResourceDigest, Container> getElements(Object resource, List<String> pathList, BaseResponseContext response)
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
        Map<ResourceDigest, Container> elements = new HashMap<>();

        String fieldName = getRelationshipName(pathList.get(0), resource.getClass());

        Object resourceProperty = PropertyUtils.getProperty(resource, fieldName);
        if (resourceProperty != null) {
            if (Iterable.class.isAssignableFrom(resourceProperty.getClass())) {
                for (Object resourceToInclude : (Iterable) resourceProperty) {
                    ResourceDigest digest = getResourceDigest(resourceToInclude);
                    //noinspection unchecked
                    elements.put(digest, new Container(resourceToInclude, response));
                }
            } else {
                ResourceDigest digest = getResourceDigest(resourceProperty);
                //noinspection unchecked
                elements.put(digest, new Container(resourceProperty, response));
            }
        } else {
            return Collections.emptyMap();
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

    private ResourceDigest getResourceDigest(Object resource) {
        Class<?> resourceClass = ClassUtils.getJsonApiResourceClass(resource);
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceClass);
        String idFieldName = registryEntry.getResourceInformation().getIdField().getUnderlyingName();
        Object idValue = PropertyUtils.getProperty(resource, idFieldName);
        String resourceType = resourceRegistry.getResourceType(resourceClass);
        return new ResourceDigest(idValue, resourceType);
    }
}
