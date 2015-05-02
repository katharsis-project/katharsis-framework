package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.Linkage;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.queryParams.RequestParams;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.Container;
import io.katharsis.response.ResourceResponse;
import org.apache.commons.beanutils.PropertyUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class ResourcePost implements BaseController {

    private ResourceRegistry resourceRegistry;

    public ResourcePost(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Check if it is a POST request for a resource.
     */
    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return jsonPath.isCollection() &&
                "POST".equals(requestType);
    }

    @Override
    public ResourceResponse handle(JsonPath jsonPath, RequestParams requestParams, RequestBody requestBody)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String resourceName = jsonPath.getResourceName();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
        if (registryEntry == null) {
            throw new ResourceNotFoundException("Resource of type not found: " + resourceName);
        }

        Object resource = buildNewResource(registryEntry, requestBody, resourceName);
        Object savedResource = registryEntry.getResourceRepository().save(resource);

        if (requestBody.getData().getLinks() != null) {
            addRelations(savedResource, registryEntry, requestBody);
        }

        Serializable resourceId = (Serializable) PropertyUtils.getProperty(savedResource, registryEntry.getResourceInformation()
                .getIdField().getName());

        Object savedResourceWithRelations = registryEntry.getResourceRepository().findOne(resourceId);

        return new ResourceResponse(new Container(savedResourceWithRelations));
    }

    private Object buildNewResource(RegistryEntry registryEntry, RequestBody requestBody, String resourceName)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        DataBody data = requestBody.getData();
        if (data == null) {
            throw new RuntimeException("No data field in the body.");
        }
        if (!resourceName.equals(data.getType())) {
            throw new RuntimeException(String.format("Non consistent type definition between path and body: body type: " +
                    "%s, request type: %s", data.getType(), resourceName));
        }

        return createBaseResource(registryEntry, requestBody);
    }

    private Object createBaseResource(RegistryEntry registryEntry, RequestBody requestBody)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        Object newInstance = registryEntry.getResourceInformation().getResourceClass().newInstance();

        for (Map.Entry<String, Object> property : requestBody.getData().getAdditionalProperties().entrySet()) {
            PropertyUtils.setProperty(newInstance, property.getKey(), property.getValue());
        }

        return newInstance;
    }

    private void addRelations(Object savedResource, RegistryEntry registryEntry, RequestBody requestBody) {
        Map<String, Linkage> additionalProperties = requestBody.getData().getLinks().getAdditionalProperties();
        for (Map.Entry<String, Linkage> property : additionalProperties.entrySet()) {
            RegistryEntry relationRegistryEntry = resourceRegistry.getEntry(property.getValue().getType());
            if (relationRegistryEntry == null) {
                throw new ResourceNotFoundException("Resource of type not found: " + property.getValue().getType());
            }
            addRelation(savedResource, registryEntry, property, relationRegistryEntry);
        }
    }

    private void addRelation(Object savedResource, RegistryEntry registryEntry, Map.Entry<String, Linkage> property, RegistryEntry relationRegistryEntry) {
        Class<?> relationshipIdClass = relationRegistryEntry.getResourceInformation().getIdField().getType();
        Serializable castedRelationshipId = castIdValue(property.getValue().getId(), relationshipIdClass);

        Class<?> relationshipClass = relationRegistryEntry.getResourceInformation().getResourceClass();
        RelationshipRepository relationshipRepository = registryEntry.getRelationshipRepositoryForClass(relationshipClass);
        relationshipRepository.addRelation(savedResource, castedRelationshipId, property.getKey());
    }

    // @TODO add more customized casting of ids
    private Serializable castIdValue(Object id, Class<?> idType) {
        if (id instanceof String) {
            if (Long.class == idType) {
                return Long.valueOf((String) id);
            } else if (Integer.class == idType) {
                return Integer.valueOf((String) id);
            }
        }
        return (Serializable) id;
    }
}
