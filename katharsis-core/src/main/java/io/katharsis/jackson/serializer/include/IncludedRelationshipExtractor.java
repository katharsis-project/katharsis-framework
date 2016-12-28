package io.katharsis.jackson.serializer.include;

import io.katharsis.queryParams.include.Inclusion;
import io.katharsis.queryParams.params.IncludedRelationsParams;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.Container;
import io.katharsis.response.ContainerType;
import io.katharsis.utils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
     * Return an ordered map from the base resource that contain the fields annotated with {@link io.katharsis.resource.annotations.JsonApiIncludeByDefault} down the chain.
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
        String topResourceType = resourceRegistry.getResourceType(resource.getClass());
        populateIncludedResources(resource, response, ContainerType.INCLUDED, includedResources, 0, topResourceType);
        LOGGER.debug("Extracted included resources {}", includedResources.toString());
        return includedResources;
    }


    private void populateIncludedResources(Object resource,
                                           BaseResponseContext response,
                                           ContainerType containerType,
                                           Map<ResourceDigest, Container> includedResourceContainers,
                                           int index,
                                           String topResourceType) {
        if (index >= 42) {
            return;
        }

        List<ResourceField> relationshipFields = getRelationshipFields(resource);
        for (ResourceField resourceField : relationshipFields) {
            if (resourceField.getIncludeByDefault() || isFieldIncluded(response, resourceField.getJsonName(), index, topResourceType)) {
                Object targetDataObj = PropertyUtils.getProperty(resource, resourceField.getUnderlyingName());
                if (targetDataObj == null) {
                    continue;
                }
                if (targetDataObj instanceof Iterable) {
                    for (Object objectItem : (Iterable) targetDataObj) {
                        if (objectItem == null) {
                            continue;
                        }
                        ResourceDigest resourceDigest = getResourceDigest(objectItem);
                        if (!includedResourceContainers.containsKey(resourceDigest)) {
                            includedResourceContainers.put(resourceDigest, new Container(objectItem, response, containerType, index + 1, topResourceType));
                        } else {
                            includedResourceContainers.get(resourceDigest).appendIncludedIndex(index + 1);
                        }
                        populateIncludedResources(objectItem, response, ContainerType.INCLUDED_NESTED, includedResourceContainers, index + 1, topResourceType);
                    }
                } else {
                    ResourceDigest resourceDigest = getResourceDigest(targetDataObj);
                    if (!includedResourceContainers.containsKey(resourceDigest)) {
                        includedResourceContainers.put(resourceDigest, new Container(targetDataObj, response, containerType, index + 1, topResourceType));
                    } else {
                        includedResourceContainers.get(resourceDigest).appendIncludedIndex(index + 1);
                    }
                    includedResourceContainers.put(getResourceDigest(targetDataObj), new Container(targetDataObj, response, containerType, index + 1, topResourceType));
                    populateIncludedResources(targetDataObj, response, ContainerType.INCLUDED_NESTED, includedResourceContainers, index + 1, topResourceType);
                }
            }
        }
    }

    private boolean isFieldIncluded(BaseResponseContext response, String fieldName, int index, String topResourceType) {
        if (response.getQueryAdapter() == null ||
                response.getQueryAdapter().getIncludedRelations() == null ||
                response.getQueryAdapter().getIncludedRelations().getParams() == null) {
            return false;
        }
        IncludedRelationsParams includedRelationsParams = response.getQueryAdapter().getIncludedRelations().getParams().get(topResourceType);
        if (includedRelationsParams == null ||
                includedRelationsParams.getParams() == null) {
            return false;
        }

        for (Inclusion inclusion : includedRelationsParams.getParams()) {
            if (inclusion.getPathList().size() > index && inclusion.getPathList().get(index).equals(fieldName)) {
                return true;
            }
        }

        return false;

    }

    private List<ResourceField> getRelationshipFields(Object resource) {
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
