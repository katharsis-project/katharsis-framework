package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.repository.RepositoryParameterProvider;
import io.katharsis.request.Request;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.LinkageData;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.resource.exception.RequestBodyException;
import io.katharsis.resource.exception.RequestBodyNotFoundException;
import io.katharsis.resource.exception.ResourceException;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.field.ResourceAttributesBridge;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.RelationshipRepositoryAdapter;
import io.katharsis.utils.Generics;
import io.katharsis.utils.PropertyUtils;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class ResourceUpsert extends BaseController {

    final ResourceRegistry resourceRegistry;
    final TypeParser typeParser;

    private final QueryParamsBuilder paramsBuilder;

    public ResourceUpsert(ResourceRegistry resourceRegistry,
                          TypeParser typeParser,
                          QueryParamsBuilder paramsBuilder,
                          ObjectMapper objectMapper) {
        super(objectMapper);
        this.resourceRegistry = resourceRegistry;
        this.typeParser = typeParser;
        this.paramsBuilder = paramsBuilder;
    }

    private static boolean allTypesTheSame(Iterable<LinkageData> linkages) {
        String type = linkages.iterator()
                .hasNext() ? linkages.iterator()
                .next()
                .getType() : null;
        for (LinkageData linkageData : linkages) {
            if (!Objects.equals(type, linkageData.getType())) {
                return false;
            }
        }
        return true;
    }

    void setId(DataBody dataBody, Object instance, RegistryEntry registryEntry) {
        if (dataBody.getId() != null) {
            String id = dataBody.getId();

            Serializable castedId = parseId(registryEntry, id);

            PropertyUtils.setProperty(instance, registryEntry.getResourceInformation().getIdField()
                    .getUnderlyingName(), castedId);
        }
    }

    void setAttributes(DataBody dataBody, Object instance, ResourceInformation resourceInformation) {
        if (dataBody.getAttributes() != null) {
            ResourceAttributesBridge resourceAttributesBridge = resourceInformation.getAttributeFields();
            resourceAttributesBridge.setProperties(objectMapper, instance, dataBody.getAttributes());
        }
    }

    protected void saveRelations(QueryParams queryParams, Object savedResource, RegistryEntry registryEntry, DataBody dataBody) {
        if (dataBody.getRelationships() != null) {
            Map<String, Object> additionalProperties = dataBody.getRelationships()
                    .getAdditionalProperties();
            for (Map.Entry<String, Object> property : additionalProperties.entrySet()) {
                if (Iterable.class.isAssignableFrom(property.getValue()
                        .getClass())) {
                    //noinspection unchecked
                    saveRelationsField(queryParams, savedResource, registryEntry, (Map.Entry) property, registryEntry
                            .getResourceInformation());
                } else {
                    //noinspection unchecked
                    saveRelationField(queryParams, savedResource, registryEntry, (Map.Entry) property, registryEntry
                            .getResourceInformation());
                }

            }
        }
    }

    private void saveRelationsField(QueryParams queryParams, Object savedResource, RegistryEntry registryEntry,
                                    Map.Entry<String, Iterable<LinkageData>> property,
                                    ResourceInformation resourceInformation) {
        if (!allTypesTheSame(property.getValue())) {
            throw new ResourceException("Not all types are the same for linkage: " + property.getKey());
        }

        String type = getLinkageType(property.getValue());
        RegistryEntry relationRegistryEntry = getRelationRegistryEntry(type);

        List<Serializable> castedRelationIds = new LinkedList<>();

        for (LinkageData linkageData : property.getValue()) {
            Serializable castedRelationshipId = parseId(registryEntry, linkageData.getId());
            castedRelationIds.add(castedRelationshipId);
        }

        Class<?> relationshipClass = relationRegistryEntry.getResourceInformation()
                .getResourceClass();
        RelationshipRepositoryAdapter relationshipRepository = registryEntry
                .getRelationshipRepositoryForClass(relationshipClass, null);
        ResourceField relationshipField = resourceInformation.findRelationshipFieldByName(property.getKey());
        //noinspection unchecked
        relationshipRepository.setRelations(savedResource, castedRelationIds,
                relationshipField.getUnderlyingName(), queryParams);
    }

    protected String getLinkageType(Iterable<LinkageData> linkages) {
        return linkages.iterator()
                .hasNext() ? linkages.iterator()
                .next()
                .getType() : null;
    }

    private void saveRelationField(QueryParams queryParams, Object savedResource, RegistryEntry registryEntry,
                                   Map.Entry<String, LinkageData> property, ResourceInformation resourceInformation) {

        RegistryEntry relationRegistryEntry = getRelationRegistryEntry(property.getValue().getType());
        Serializable castedRelationshipId = parseId(relationRegistryEntry, property.getValue().getId());

        Class<?> relationshipClass = relationRegistryEntry.getResourceInformation().getResourceClass();

        RelationshipRepositoryAdapter relationshipRepository = registryEntry
                .getRelationshipRepositoryForClass(relationshipClass, null);

        ResourceField relationshipField = resourceInformation.findRelationshipFieldByName(property.getKey());
        //noinspection unchecked
        relationshipRepository.setRelation(savedResource, castedRelationshipId, relationshipField.getUnderlyingName(),
                queryParams);
    }

    private RegistryEntry getRelationRegistryEntry(String type) {
        RegistryEntry relationRegistryEntry = resourceRegistry.getEntry(type);
        if (relationRegistryEntry == null) {
            throw new ResourceNotFoundException(type);
        }
        return relationRegistryEntry;
    }

    Object buildNewResource(RegistryEntry registryEntry, DataBody dataBody, String resourceName) {
        if (dataBody == null) {
            throw new ResourceException("No data field in the body.");
        }
        if (!resourceName.equals(dataBody.getType())) {
            throw new ResourceException(String.format("Inconsistent type definition between path and body: body type: " +
                    "%s, request type: %s", dataBody.getType(), resourceName));
        }
        try {
            return registryEntry.getResourceInformation()
                    .getResourceClass()
                    .newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ResourceException(
                    String.format("couldn't create a new instance of %s", registryEntry.getResourceInformation()
                            .getResourceClass()));
        }
    }

    protected void setRelations(Object newResource,
                                RegistryEntry registryEntry,
                                DataBody dataBody,
                                QueryParams queryParams,
                                RepositoryParameterProvider parameterProvider) {
        if (dataBody.getRelationships() != null) {
            Map<String, Object> additionalProperties = dataBody.getRelationships()
                    .getAdditionalProperties();
            for (Map.Entry<String, Object> property : additionalProperties.entrySet()) {
                if (property.getValue() != null && Iterable.class.isAssignableFrom(property.getValue()
                        .getClass())) {
                    //noinspection unchecked
                    setRelationsField(newResource, registryEntry, (Map.Entry) property, queryParams, parameterProvider);
                } else {
                    //noinspection unchecked
                    setRelationField(newResource, registryEntry, (Map.Entry) property, queryParams, parameterProvider);
                }

            }
        }
    }

    private void setRelationsField(Object newResource, RegistryEntry registryEntry,
                                   Map.Entry<String, Iterable<LinkageData>> property,
                                   QueryParams queryParams,
                                   RepositoryParameterProvider parameterProvider) {
        String propertyName = property.getKey();
        ResourceField relationshipField = registryEntry.getResourceInformation()
                .findRelationshipFieldByName(propertyName);
        Class<?> relationshipFieldClass = Generics.getResourceClass(relationshipField.getGenericType(),
                relationshipField.getType());
        RegistryEntry entry = resourceRegistry.getEntry(relationshipFieldClass);

        List relationships = new LinkedList<>();
        for (LinkageData linkageData : property.getValue()) {
            Serializable castedRelationshipId = parseId(entry, linkageData.getId());

            Object relationObject = entry.getResourceRepository(parameterProvider)
                    .findOne(castedRelationshipId, queryParams)
                    .getEntity();
            relationships.add(relationObject);
        }
        PropertyUtils.setProperty(newResource, relationshipField.getUnderlyingName(), relationships);
    }

    private void setRelationField(Object newResource, RegistryEntry registryEntry,
                                  Map.Entry<String, LinkageData> property,
                                  QueryParams queryParams,
                                  RepositoryParameterProvider parameterProvider) {

        ResourceField relationshipFieldByName = registryEntry.getResourceInformation()
                .findRelationshipFieldByName(property.getKey());

        if(relationshipFieldByName == null) {
            throw new ResourceException(String.format("Invalid relationship name: %s", property.getKey()));
        }

        Object relationObject;
        if (property.getValue() != null) {
            RegistryEntry entry = resourceRegistry.getEntry(relationshipFieldByName.getType());

            Serializable castedRelationshipId = parseId(entry, property.getValue().getId());

            relationObject = entry.getResourceRepository(parameterProvider)
                    .findOne(castedRelationshipId, queryParams)
                    .getEntity();
        } else {
            relationObject = null;
        }

        PropertyUtils.setProperty(newResource, relationshipFieldByName.getUnderlyingName(), relationObject);
    }

    @Override
    public TypeParser getTypeParser() {
        return typeParser;
    }

    protected DataBody dataBody(Request request) {

        if (!request.getBody().isPresent()) {
            throw new RequestBodyNotFoundException(request.getMethod(), request.getPath().getResource());
        }
        RequestBody body = parseBody(request.getBody().get());

        if (body.isMultiple()) {
            throw new RequestBodyException(request.getMethod(), request.getPath().getResource(),
                    "Multiple data in body");
        }

        DataBody dataBody = body.getSingleData();
        if (dataBody == null) {
            throw new RequestBodyException(request.getMethod(), request.getPath().getResource(), "No data field in the body.");
        }
        return dataBody;
    }

    @Override
    public QueryParamsBuilder getQueryParamsBuilder() {
        return paramsBuilder;
    }
}
