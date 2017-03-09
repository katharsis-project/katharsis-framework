package io.katharsis.core.internal.dispatcher.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.core.internal.dispatcher.path.JsonPath;
import io.katharsis.core.internal.dispatcher.path.ResourcePath;
import io.katharsis.core.internal.repository.adapter.ResourceRepositoryAdapter;
import io.katharsis.core.internal.resource.DocumentMapper;
import io.katharsis.errorhandling.exception.RepositoryNotFoundException;
import io.katharsis.errorhandling.exception.RequestBodyException;
import io.katharsis.errorhandling.exception.RequestBodyNotFoundException;
import io.katharsis.errorhandling.exception.ResourceException;
import io.katharsis.errorhandling.exception.ResourceNotFoundException;
import io.katharsis.legacy.internal.RepositoryMethodParameterProvider;
import io.katharsis.repository.request.HttpMethod;
import io.katharsis.repository.request.QueryAdapter;
import io.katharsis.repository.response.JsonApiResponse;
import io.katharsis.repository.response.Response;
import io.katharsis.resource.Document;
import io.katharsis.resource.Relationship;
import io.katharsis.resource.Resource;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.parser.TypeParser;

public class ResourcePatch extends ResourceUpsert {

    public ResourcePatch(ResourceRegistry resourceRegistry, TypeParser typeParser, @SuppressWarnings("SameParameterValue") ObjectMapper objectMapper, DocumentMapper documentMapper) {
        super(resourceRegistry, typeParser, objectMapper, documentMapper);
    }

    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection() &&
                jsonPath instanceof ResourcePath &&
                HttpMethod.PATCH.name().equals(requestType);
    }

    @Override
    public Response handle(JsonPath jsonPath, QueryAdapter queryAdapter,
                                         RepositoryMethodParameterProvider parameterProvider, Document requestDocument) {

        String resourceEndpointName = jsonPath.getResourceName();
        RegistryEntry endpointRegistryEntry = resourceRegistry.getEntry(resourceEndpointName);
        if (endpointRegistryEntry == null) {
            throw new ResourceNotFoundException(resourceEndpointName);
        }
        if (requestDocument == null) {
            throw new RequestBodyNotFoundException(HttpMethod.PATCH, resourceEndpointName);
        }
        if (requestDocument.getData() instanceof Collection) {
            throw new RequestBodyException(HttpMethod.PATCH, resourceEndpointName, "Multiple data in body");
        }

        String idString = jsonPath.getIds().getIds().get(0);

        Resource resourceBody = (Resource) requestDocument.getData().get();
        if (resourceBody == null) {
            throw new RequestBodyException(HttpMethod.POST, resourceEndpointName, "No data field in the body.");
        }
        RegistryEntry bodyRegistryEntry = resourceRegistry.getEntry(resourceBody.getType());
        if(bodyRegistryEntry == null){
        	throw new RepositoryNotFoundException(resourceBody.getType());
        }
        ResourceInformation resourceInformation = bodyRegistryEntry.getResourceInformation();
        verifyTypes(HttpMethod.PATCH, resourceEndpointName, endpointRegistryEntry, bodyRegistryEntry);

        Class<?> type = bodyRegistryEntry
            .getResourceInformation()
            .getIdField()
            .getType();
        Serializable resourceId = resourceInformation.parseIdString(idString);

        ResourceRepositoryAdapter resourceRepository = endpointRegistryEntry.getResourceRepository(parameterProvider);
        JsonApiResponse resourceFindResponse = resourceRepository.findOne(resourceId, queryAdapter);
        Object resource = extractResource(resourceFindResponse);
        if(resource == null){
        	throw new ResourceNotFoundException(jsonPath.toString());
        }
        Resource resourceFindData = documentMapper.toDocument(resourceFindResponse, queryAdapter, parameterProvider).getSingleData().get();

        resourceInformation.verify(resource, requestDocument);
        
        // extract current attributes from findOne without any manipulation by query params (such as sparse fieldsets)
        try{
        	String attributesFromFindOne = extractAttributesFromResourceAsJson(resourceFindData);
	        Map<String,Object> attributesToUpdate = new HashMap<>(emptyIfNull(objectMapper.readValue(attributesFromFindOne, Map.class)));
	      
	        // deserialize the request JSON's attributes object into a map
	        String attributesAsJson = objectMapper.writeValueAsString(resourceBody.getAttributes());
	        Map<String,Object> attributesFromRequest = emptyIfNull(objectMapper.readValue(attributesAsJson, Map.class));
   
	        // remove attributes that were omitted in the request
	        Iterator<String> it = attributesToUpdate.keySet().iterator();
	        while(it.hasNext()) {
	            String key = it.next();
	            if(!attributesFromRequest.containsKey(key)){
	                it.remove();
	            }
	        }
	
	        // walk the source map and apply target values from request
	        updateValues(attributesToUpdate, attributesFromRequest);
	        Map<String,JsonNode> upsertedAttributes = new HashMap<>();
	        for(Map.Entry<String, Object> entry : attributesToUpdate.entrySet()){
	        	 JsonNode value = objectMapper.valueToTree(entry.getValue());
	        	 upsertedAttributes.put(entry.getKey(), value);
	        }   
	        
	        resourceBody.setAttributes(upsertedAttributes);
        }catch(IOException e){
        	throw new ResourceException("failed to merge patched attributes", e);
        }

        setAttributes(resourceBody, resource, bodyRegistryEntry.getResourceInformation());
        setRelations(resource, bodyRegistryEntry, resourceBody, queryAdapter, parameterProvider);
        
        Set<String> loadedRelationshipNames = getLoadedRelationshipNames(resourceBody);
        
        JsonApiResponse updatedResource = resourceRepository.update(resource, queryAdapter);
        Document responseDocument = documentMapper.toDocument(updatedResource, queryAdapter, parameterProvider, loadedRelationshipNames);

        return new Response(responseDocument, 200);
    }

	private <K,V> Map<K,V> emptyIfNull(Map<K,V> value) {
		return (Map<K, V>) (value != null ? value : Collections.emptyMap());
	}

	private String extractAttributesFromResourceAsJson(Resource resource) throws IOException{

        JsonApiResponse response = new JsonApiResponse();
        response.setEntity(resource);
        // deserialize using the objectMapper so it becomes json-api
        String newRequestBody = objectMapper.writeValueAsString(resource);
        JsonNode node = objectMapper.readTree(newRequestBody);
        JsonNode attributes = node.findValue("attributes");
        return objectMapper.writeValueAsString(attributes);

    }

    private void updateValues(Map<String, Object> source, Map<String, Object> updates) {

        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String fieldName = entry.getKey();
            Object updatedValue = entry.getValue();

            // updating an embedded object
            if (updatedValue instanceof Map) {

                // source may lack the whole entry yet
                if (source.get(fieldName) == null) {
                    source.put(fieldName, new HashMap<>());
                }

                Object sourceMap = source.get(fieldName);
                updateValues((Map<String, Object>)sourceMap, (Map<String, Object>)updatedValue);
                continue;
            }

            // updating a simple value
            source.put(fieldName, updatedValue);
        }
    }

}
