package io.katharsis.dispatcher.controller.resource;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.resource.Relationship;
import io.katharsis.resource.Resource;
import io.katharsis.resource.ResourceId;
import io.katharsis.resource.exception.ResourceException;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.field.ResourceAttributesBridge;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.information.ResourceInstanceBuilder;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.repository.adapter.RelationshipRepositoryAdapter;
import io.katharsis.utils.Generics;
import io.katharsis.utils.PropertyUtils;
import io.katharsis.utils.parser.TypeParser;

public abstract class ResourceUpsert extends BaseController {
    final ResourceRegistry resourceRegistry;
    final TypeParser typeParser;
    protected final ObjectMapper objectMapper;

    public ResourceUpsert(ResourceRegistry resourceRegistry, TypeParser typeParser, ObjectMapper objectMapper) {
        this.resourceRegistry = resourceRegistry;
        this.typeParser = typeParser;
        this.objectMapper = objectMapper;
    }

    protected Object newResource(ResourceInformation resourceInformation, Resource dataBody) {
        ResourceInstanceBuilder<?> builder = resourceInformation.getInstanceBuilder();
        return builder.buildResource(dataBody);
    }

    protected void setId(Resource dataBody, Object instance, ResourceInformation resourceInformation) {
        if (dataBody.getId() != null) {
            String id = dataBody.getId();

            Serializable castedId = resourceInformation.parseIdString(id);
            PropertyUtils.setProperty(instance, resourceInformation.getIdField()
                    .getUnderlyingName(), castedId);
        }
    }

    protected void setAttributes(Resource dataBody, Object instance, ResourceInformation resourceInformation) {
        if (dataBody.getAttributes() != null) {
            ResourceAttributesBridge resourceAttributesBridge = resourceInformation.getAttributeFields();
            resourceAttributesBridge.setProperties(objectMapper, instance, dataBody.getAttributes());
        }
    }

    protected void saveRelations(QueryAdapter queryAdapter, Object savedResource, RegistryEntry registryEntry, Resource dataBody,
                                 RepositoryMethodParameterProvider parameterProvider) {
        if (dataBody.getRelationships() != null) {
            for (Map.Entry<String, Relationship> property : dataBody.getRelationships().entrySet()) {
            	Object data = property.getValue().getData();
                if (data instanceof Iterable) {
                    //noinspection unchecked
                    saveRelationsField(queryAdapter, savedResource, registryEntry, (Map.Entry) property, registryEntry
                            .getResourceInformation(), parameterProvider);
                } else {
                    //noinspection unchecked
                    saveRelationField(queryAdapter, savedResource, registryEntry, (Map.Entry) property, registryEntry
                            .getResourceInformation(), parameterProvider);
                }

            }
        }
    }

    private void saveRelationsField(QueryAdapter queryAdapter, Object savedResource, RegistryEntry registryEntry,
                                    Map.Entry<String, Iterable<ResourceId>> property,
                                    ResourceInformation resourceInformation,
                                    RepositoryMethodParameterProvider parameterProvider) {
        if (!allTypesTheSame(property.getValue())) {
            throw new ResourceException("Not all types are the same for linkage: " + property.getKey());
        }

        String type = getLinkageType(property.getValue());
        RegistryEntry relationRegistryEntry = getRelationRegistryEntry(type);
        @SuppressWarnings("unchecked")
        Class<? extends Serializable> relationshipIdClass = (Class<? extends Serializable>) relationRegistryEntry
                .getResourceInformation()
                .getIdField()
                .getType();
        List<Serializable> castedRelationIds = new LinkedList<>();

        for (ResourceId linkageData : property.getValue()) {
            Serializable castedRelationshipId = typeParser.parse(linkageData.getId(), relationshipIdClass);
            castedRelationIds.add(castedRelationshipId);
        }

        Class<?> relationshipClass = relationRegistryEntry.getResourceInformation()
                .getResourceClass();
        RelationshipRepositoryAdapter relationshipRepository = registryEntry
                .getRelationshipRepositoryForClass(relationshipClass, parameterProvider);
        ResourceField relationshipField = resourceInformation.findRelationshipFieldByName(property.getKey());
        //noinspection unchecked
        relationshipRepository.setRelations(savedResource, castedRelationIds,
                relationshipField.getUnderlyingName(), queryAdapter);
    }

    private static boolean allTypesTheSame(Iterable<ResourceId> linkages) {
        String type = linkages.iterator()
                .hasNext() ? linkages.iterator()
                .next()
                .getType() : null;
        for (ResourceId linkageData : linkages) {
            if (!Objects.equals(type, linkageData.getType())) {
                return false;
            }
        }
        return true;
    }

    protected String getLinkageType(Iterable<ResourceId> linkages) {
        return linkages.iterator()
                .hasNext() ? linkages.iterator()
                .next()
                .getType() : null;
    }

    private void saveRelationField(QueryAdapter queryAdapter, Object savedResource, RegistryEntry registryEntry,
                                   Map.Entry<String, ResourceId> property, ResourceInformation resourceInformation,
                                   RepositoryMethodParameterProvider parameterProvider) {
        RegistryEntry relationRegistryEntry = getRelationRegistryEntry(property.getValue()
                .getType());

        @SuppressWarnings("unchecked")
        Class<? extends Serializable> relationshipIdClass = (Class<? extends Serializable>) relationRegistryEntry
                .getResourceInformation()
                .getIdField()
                .getType();
        Serializable castedRelationshipId = typeParser.parse(property.getValue()
                .getId(), relationshipIdClass);

        Class<?> relationshipClass = relationRegistryEntry.getResourceInformation()
                .getResourceClass();
        RelationshipRepositoryAdapter relationshipRepository = registryEntry
                .getRelationshipRepositoryForClass(relationshipClass, parameterProvider);
        ResourceField relationshipField = resourceInformation.findRelationshipFieldByName(property.getKey());
        //noinspection unchecked
        relationshipRepository.setRelation(savedResource, castedRelationshipId, relationshipField.getUnderlyingName(),
                queryAdapter);
    }

    private RegistryEntry getRelationRegistryEntry(String type) {
        RegistryEntry relationRegistryEntry = resourceRegistry.getEntry(type);
        if (relationRegistryEntry == null) {
            throw new ResourceNotFoundException(type);
        }
        return relationRegistryEntry;
    }

    Object buildNewResource(RegistryEntry registryEntry, Resource dataBody, String resourceName) {
        if (dataBody == null) {
            throw new ResourceException("No data field in the body.");
        }
        if (!resourceName.equals(dataBody.getType())) {
            throw new ResourceException(String.format("Inconsistent type definition between path and body: body type: " +
                            "%s, request type: %s",
                    dataBody.getType(),
                    resourceName));
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

    protected void setRelations(Object newResource, RegistryEntry registryEntry, Resource dataBody, QueryAdapter
            queryAdapter,
                                RepositoryMethodParameterProvider parameterProvider) {
        if (dataBody.getRelationships() != null) {
            for (Map.Entry<String, Relationship> property : dataBody.getRelationships().entrySet()) {
            	String propertyName = property.getKey();
            	
            	ResourceInformation resourceInformation = registryEntry.getResourceInformation();
				ResourceField field = resourceInformation.findRelationshipFieldByName(propertyName);
				if(field == null){
					 throw new ResourceException(String.format("Invalid relationship name: %s for %s", property.getKey(), resourceInformation.getResourceType()));
				}
            	if (Iterable.class.isAssignableFrom(field.getType())){
                    //noinspection unchecked
                    setRelationsField(newResource,
                            registryEntry,
                            (Map.Entry) property,
                            queryAdapter,
                            parameterProvider);
                } else {
                    //noinspection unchecked
                    setRelationField(newResource, registryEntry, propertyName, (ResourceId)property.getValue().getData(), queryAdapter, parameterProvider);
                }

            }
        }
    }

    protected void setRelationsField(Object newResource, RegistryEntry registryEntry,
                                   Map.Entry<String, Iterable<ResourceId>> property, QueryAdapter queryAdapter,
                                   RepositoryMethodParameterProvider parameterProvider) {
    	if(property.getValue() != null){
	        String propertyName = property.getKey();
	        ResourceField relationshipField = registryEntry.getResourceInformation()
	                .findRelationshipFieldByName(propertyName);
	        Class<?> relationshipFieldClass = Generics.getResourceClass(relationshipField.getGenericType(),
	                relationshipField.getType());
	        RegistryEntry entry = null;
	        Class idFieldType = null;
	        List relationships = new LinkedList<>();
	        boolean first = true;
	        
	        for (ResourceId linkageData : property.getValue()) {
	            if (first) {
	                entry = resourceRegistry.getEntry(linkageData.getType(), relationshipFieldClass);
	                idFieldType = entry.getResourceInformation()
	                        .getIdField()
	                        .getType();
	                first = false;
	            }
	            Serializable castedRelationshipId = typeParser.parse(linkageData.getId(), idFieldType);
	            
	            Object relationObject = fetchRelatedObject(entry, castedRelationshipId, parameterProvider, queryAdapter);
	            
	            relationships.add(relationObject);
	        }
	        PropertyUtils.setProperty(newResource, relationshipField.getUnderlyingName(), relationships);
    	}
    }

    protected void setRelationField(Object newResource, RegistryEntry registryEntry,
                                  String relationshipName, ResourceId relationshipId, QueryAdapter queryAdapter,
                                  RepositoryMethodParameterProvider parameterProvider) {

        ResourceField relationshipFieldByName = registryEntry.getResourceInformation()
                .findRelationshipFieldByName(relationshipName);

        if (relationshipFieldByName == null) {
            throw new ResourceException(String.format("Invalid relationship name: %s", relationshipName));
        }

        Object relationObject;
        if (relationshipId != null) {
            RegistryEntry entry = resourceRegistry.getEntry(relationshipId.getType(),
                    relationshipFieldByName.getType());
            Class idFieldType = entry.getResourceInformation()
                    .getIdField()
                    .getType();
            Serializable castedRelationshipId = typeParser.parse(relationshipId.getId(), idFieldType);
            
            relationObject = fetchRelatedObject(entry, castedRelationshipId, parameterProvider, queryAdapter);
        } else {
            relationObject = null;
        }

        PropertyUtils.setProperty(newResource, relationshipFieldByName.getUnderlyingName(), relationObject);
    }

	protected Object fetchRelatedObject(RegistryEntry entry, Serializable relationId, RepositoryMethodParameterProvider parameterProvider,
			QueryAdapter queryAdapter) {
		return entry.getResourceRepository(parameterProvider).findOne(relationId, queryAdapter).getEntity();
	}
}
