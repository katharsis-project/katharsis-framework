package io.katharsis.jackson.serializer.include;

import io.katharsis.queryParams.include.Inclusion;
import io.katharsis.queryParams.params.IncludedRelationsParams;
import io.katharsis.queryParams.params.TypedParams;
import io.katharsis.request.path.FieldPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.exception.ResourceFieldNotFoundException;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.Container;
import io.katharsis.response.ContainerType;
import io.katharsis.utils.PropertyUtils;
import io.katharsis.utils.java.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Extracts inclusions from a resource.
 */
public class IncludedRelationshipExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(IncludedRelationshipExtractor.class);
    private final ResourceRegistry resourceRegistry;

    public IncludedRelationshipExtractor(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    /**
     * Return an ordered map from the base resource that contain the fields annotated with {@link io.katharsis.resource.annotations.JsonApiIncludeByDefault} up to the first level of inclusions.
     * <p>
     * Additionally, return all the fields that are nested N levels deep in the include query parameters.
     * ex. include=projects.task.project will go three levels deep trying to find populated fields.
     *
     * @param resource
     * @param response
     * @return
     */
    public Map<ResourceDigest, Container> extractIncludedResources(Object resource, BaseResponseContext response) {
        Map<ResourceDigest, Container> includedResources = new LinkedHashMap<>();

        populateIncludedByDefaultResources(resource, response, ContainerType.TOP, includedResources, 1);
        populateIncludedRelationships(resource, response, includedResources);
        LOGGER.debug("Extracted included resources {}", includedResources.toString());
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
            if ((containerType.equals(ContainerType.TOP) || containerType.equals(ContainerType.INCLUDED_DEFAULT))
                    && resourceField.getIncludeByDefault()) {
                recurrenceLevelCounter++;
                populateIncludedByDefaultResourcesRecursive(targetDataObj, response, containerType, includedResources, recurrenceLevelCounter, resourceField);
            } // if this is a top level container and its field matches the included parameters traverse further to find defaults
            else if (containerType.equals(ContainerType.TOP) && isFieldIncluded(response, resourceField.getUnderlyingName())) {
                recurrenceLevelCounter++;
                populateIncludedByDefaultResourcesRecursive(targetDataObj, response, containerType, includedResources, recurrenceLevelCounter, resourceField);
            }
        }
    }

    private boolean isFieldIncluded(BaseResponseContext response, String fieldName) {
        if (response.getQueryAdapter() == null ||
                response.getQueryAdapter().getIncludedRelations() == null ||
                response.getQueryAdapter().getIncludedRelations().getParams() == null) {
            return false;
        }
        IncludedRelationsParams includedRelationsParams = response.getQueryAdapter().getIncludedRelations().getParams().get(response.getJsonPath().getElementName());
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
                                                             int recurrenceLevelCounter,
                                                             ResourceField resourceField) {
        if (targetDataObj instanceof Iterable) {
            for (Object objectItem : (Iterable) targetDataObj) {
                if (objectItem == null) {
                    continue;
                }
                if (containerType.equals(ContainerType.TOP)) {
                    includedResourceContainers.put(getResourceDigest(objectItem), new Container(objectItem, response, containerType, resourceField.getJsonName(), 0, null));
                    populateIncludedByDefaultResources(objectItem, response, ContainerType.INCLUDED_DEFAULT, includedResourceContainers, recurrenceLevelCounter);
                } else if (containerType.equals(ContainerType.INCLUDED_DEFAULT)) {
                    includedResourceContainers.put(getResourceDigest(objectItem), new Container(objectItem, response, containerType, resourceField.getJsonName(), 1, null));
                    populateIncludedByDefaultResources(objectItem, response, ContainerType.INCLUDED_DEFAULT_NESTED, includedResourceContainers, recurrenceLevelCounter);
                }
            }
        } else {
            includedResourceContainers.put(getResourceDigest(targetDataObj), new Container(targetDataObj, response, containerType));
            if (containerType.equals(ContainerType.TOP)) {
                includedResourceContainers.put(getResourceDigest(targetDataObj), new Container(targetDataObj, response, containerType, resourceField.getJsonName(), 0, null));
                populateIncludedByDefaultResources(targetDataObj, response, ContainerType.INCLUDED_DEFAULT, includedResourceContainers, recurrenceLevelCounter);
            } else if (containerType.equals(ContainerType.INCLUDED_DEFAULT)) {
                includedResourceContainers.put(getResourceDigest(targetDataObj), new Container(targetDataObj, response, containerType, resourceField.getJsonName(), 1, null));
                populateIncludedByDefaultResources(targetDataObj, response, ContainerType.INCLUDED_DEFAULT_NESTED, includedResourceContainers, recurrenceLevelCounter);
            }
        }

    }

    private void populateIncludedRelationships(Object resource, BaseResponseContext response, Map<ResourceDigest, Container> includedResources) {
        if (response.getQueryAdapter() == null || response.getJsonPath() == null) {
            return;
        }
        TypedParams<IncludedRelationsParams> includedRelations = response.getQueryAdapter()
                .getIncludedRelations();
        String elementName = response.getJsonPath()
                .getElementName();

        // handle field paths differently because the element name is not its type but field name (#357)
        if (response.getJsonPath() instanceof FieldPath) {
            // extract the resource's resource type name
            Optional<Class<?>> optional = resourceRegistry.getResourceClass(resource);
            if (optional.isPresent()) {
                elementName = resourceRegistry.getResourceType(optional.get());
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

    private void populateIncludedRelationship(Object resource, Inclusion inclusion, BaseResponseContext response, Map<ResourceDigest, Container> includedResources) {
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
        populateIncludedResources(resource, pathList, response, includedResources, 0);
    }

    private void populateIncludedResources(Object resource, List<String> pathList, BaseResponseContext response,
                                           Map<ResourceDigest, Container> includedResources, int index) {
        if (index == pathList.size()) {
            return;
        }
        String fieldName = getRelationshipName(pathList.get(index), resource.getClass());
        Object resourceProperty = PropertyUtils.getProperty(resource, fieldName);
        if (resourceProperty == null) {
            return;
        }
        if (Iterable.class.isAssignableFrom(resourceProperty.getClass())) {
            for (Object resourceToInclude : (Iterable) resourceProperty) {
                createOrAppendFieldName(includedResources, index, resourceToInclude, pathList, response);
                populateIncludedResources(resourceToInclude, pathList, response, includedResources, index + 1);
            }
        } else {
            createOrAppendFieldName(includedResources, index, resourceProperty, pathList, response);
            populateIncludedResources(resourceProperty, pathList, response, includedResources, index + 1);
        }
    }

    private void createOrAppendFieldName(Map<ResourceDigest, Container> includedResources, int index, Object resourceToInclude, List<String> pathList, BaseResponseContext response) {
        ResourceDigest digest = getResourceDigest(resourceToInclude);
        if (!includedResources.containsKey(digest)) {
            if (index == 0) {
                includedResources.put(digest, new Container(resourceToInclude, response, ContainerType.INCLUDED, pathList.get(index), index, pathList));
            } else {
                includedResources.put(digest, new Container(resourceToInclude, response, ContainerType.INCLUDED_NESTED, pathList.get(index), index, pathList));
            }
        } else {
            // if the object has already been added then lets confirm this field name will be serialized too
            index = index + 1;
            if (pathList.size() > index) {
                Container container = includedResources.get(digest);
                String fieldName = pathList.get(index);
                container.appendAdditionalFields(fieldName);
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
        Class<?> resourceClass = resourceRegistry.getResourceClass(resource).get();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceClass);
        String idFieldName = registryEntry.getResourceInformation().getIdField().getUnderlyingName();
        Object idValue = PropertyUtils.getProperty(resource, idFieldName);
        String resourceType = resourceRegistry.getResourceType(resourceClass);
        return new ResourceDigest(idValue, resourceType);
    }
}
