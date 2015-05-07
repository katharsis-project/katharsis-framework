package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.Linkage;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.Container;
import io.katharsis.response.ResourceResponse;
import io.katharsis.utils.Generics;
import org.apache.commons.beanutils.PropertyUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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


        for (Map.Entry<String, Object> property : requestBody.getData().getAttributes().getAttributes().entrySet()) {
            PropertyUtils.setProperty(newInstance, property.getKey(), property.getValue());
        }

        return newInstance;
    }

    private void addRelations(Object savedResource, RegistryEntry registryEntry, RequestBody requestBody)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Map<String, Object> additionalProperties = requestBody.getData().getLinks().getAdditionalProperties();
        for (Map.Entry<String, Object> property : additionalProperties.entrySet()) {
            if (Iterable.class.isAssignableFrom(property.getValue().getClass())) {
                addRelationsField(savedResource, registryEntry, (Map.Entry) property);
            } else {
                addRelationField(savedResource, registryEntry, (Map.Entry) property);
            }

        }
    }

    private void addRelationsField(Object savedResource, RegistryEntry registryEntry, Map.Entry<String,
            Iterable<Linkage>> property) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (!allTypesTheSame(property.getValue())) {
            throw new RuntimeException("Not all types are the same for linkage: " + property.getKey());
        }

        String type = getLinkageType(property.getValue());
        RegistryEntry relationRegistryEntry = getRelationRegistryEntry(type);
        Class<?> relationshipIdClass = relationRegistryEntry.getResourceInformation().getIdField().getType();
        List<Serializable> castedRelationIds = new LinkedList<>();

        for (Linkage linkage : property.getValue()) {
            Serializable castedRelationshipId = Generics.castIdValue(linkage.getId(), relationshipIdClass);
            castedRelationIds.add(castedRelationshipId);
        }

        Class<?> relationshipClass = relationRegistryEntry.getResourceInformation().getResourceClass();
        RelationshipRepository relationshipRepository = registryEntry.getRelationshipRepositoryForClass(relationshipClass);
        relationshipRepository.setRelations(savedResource, castedRelationIds, property.getKey());
    }

    private boolean allTypesTheSame(Iterable<Linkage> linkages) {
        String type = linkages.iterator().hasNext() ? linkages.iterator().next().getType() : null;
        for (Linkage linkage : linkages) {
            if (!Objects.equals(type, linkage.getType())) {
                return false;
            }
        }
        return true;
    }

    private String getLinkageType(Iterable<Linkage> linkages) {
        return linkages.iterator().hasNext() ? linkages.iterator().next().getType() : null;
    }

    private void addRelationField(Object savedResource, RegistryEntry registryEntry, Map.Entry<String, Linkage> property)
            throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        RegistryEntry relationRegistryEntry = getRelationRegistryEntry(property.getValue().getType());

        Class<?> relationshipIdClass = relationRegistryEntry.getResourceInformation().getIdField().getType();
        Serializable castedRelationshipId = Generics.castIdValue(property.getValue().getId(), relationshipIdClass);

        Class<?> relationshipClass = relationRegistryEntry.getResourceInformation().getResourceClass();
        RelationshipRepository relationshipRepository = registryEntry.getRelationshipRepositoryForClass(relationshipClass);
        relationshipRepository.setRelation(savedResource, castedRelationshipId, property.getKey());
    }

    private RegistryEntry getRelationRegistryEntry(String type) {
        RegistryEntry relationRegistryEntry = resourceRegistry.getEntry(type);
        if (relationRegistryEntry == null) {
            throw new ResourceNotFoundException("Resource of type not found: " + type);
        }
        return relationRegistryEntry;
    }
}
