package io.katharsis.dispatcher.controller.resource;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.dispatcher.controller.Response;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.Document;
import io.katharsis.resource.Resource;
import io.katharsis.resource.exception.RequestBodyException;
import io.katharsis.resource.exception.RequestBodyNotFoundException;
import io.katharsis.resource.exception.ResourceException;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.internal.DocumentMapper;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.repository.adapter.ResourceRepositoryAdapter;
import io.katharsis.response.JsonApiResponse;
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

        Resource resourceBody = (Resource) requestDocument.getData();
        if (resourceBody == null) {
            throw new RequestBodyException(HttpMethod.POST, resourceEndpointName, "No data field in the body.");
        }
        RegistryEntry bodyRegistryEntry = resourceRegistry.getEntry(resourceBody.getType());
        verifyTypes(HttpMethod.PATCH, resourceEndpointName, endpointRegistryEntry, bodyRegistryEntry);

        Class<?> type = bodyRegistryEntry
            .getResourceInformation()
            .getIdField()
            .getType();
        Serializable resourceId = typeParser.parse(idString, (Class<? extends Serializable>) type);

        ResourceRepositoryAdapter resourceRepository = endpointRegistryEntry.getResourceRepository(parameterProvider);
        JsonApiResponse resourceFindResponse = resourceRepository.findOne(resourceId, queryAdapter);
        Object resource = extractResource(resourceFindResponse);
        Resource resourceFindData = (Resource) documentMapper.toDocument(resourceFindResponse, queryAdapter).getData();

        // extract current attributes from findOne without any manipulation by query params (such as sparse fieldsets)
        try{
        	String attributesFromFindOne = extractAttributesFromResourceAsJson(resourceFindData);
	        Map<String,Object> attributesToUpdate = emptyIfNull(objectMapper.readValue(attributesFromFindOne, Map.class));
	      
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
        Document responseDocument = documentMapper.toDocument(resourceRepository.update(resource, queryAdapter), queryAdapter);

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
                if (!source.containsKey(fieldName)) {
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
