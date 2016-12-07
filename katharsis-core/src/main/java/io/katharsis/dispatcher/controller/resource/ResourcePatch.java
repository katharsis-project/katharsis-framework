package io.katharsis.dispatcher.controller.resource;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.exception.RequestBodyException;
import io.katharsis.resource.exception.RequestBodyNotFoundException;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.repository.adapter.ResourceRepositoryAdapter;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.ResourceResponseContext;
import io.katharsis.utils.parser.TypeParser;

public class ResourcePatch extends ResourceUpsert {

    public ResourcePatch(ResourceRegistry resourceRegistry, TypeParser typeParser, @SuppressWarnings("SameParameterValue") ObjectMapper objectMapper) {
        super(resourceRegistry, typeParser, objectMapper);
    }

    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection() &&
                jsonPath instanceof ResourcePath &&
                HttpMethod.PATCH.name().equals(requestType);
    }

    @Override
    public BaseResponseContext handle(JsonPath jsonPath, QueryAdapter queryAdapter,
                                         RepositoryMethodParameterProvider parameterProvider, RequestBody requestBody) {

        String resourceEndpointName = jsonPath.getResourceName();
        RegistryEntry endpointRegistryEntry = resourceRegistry.getEntry(resourceEndpointName);
        if (endpointRegistryEntry == null) {
            throw new ResourceNotFoundException(resourceEndpointName);
        }
        if (requestBody == null) {
            throw new RequestBodyNotFoundException(HttpMethod.PATCH, resourceEndpointName);
        }
        if (requestBody.isMultiple()) {
            throw new RequestBodyException(HttpMethod.PATCH, resourceEndpointName, "Multiple data in body");
        }

        String idString = jsonPath.getIds().getIds().get(0);

        DataBody dataBody = requestBody.getSingleData();
        if (dataBody == null) {
            throw new RequestBodyException(HttpMethod.POST, resourceEndpointName, "No data field in the body.");
        }
        RegistryEntry bodyRegistryEntry = resourceRegistry.getEntry(dataBody.getType());
        verifyTypes(HttpMethod.PATCH, resourceEndpointName, endpointRegistryEntry, bodyRegistryEntry);

        Class<?> type = bodyRegistryEntry
            .getResourceInformation()
            .getIdField()
            .getType();
        Serializable resourceId = typeParser.parse(idString, (Class<? extends Serializable>) type);

        ResourceRepositoryAdapter resourceRepository = endpointRegistryEntry.getResourceRepository(parameterProvider);
        @SuppressWarnings("unchecked")
        Object resource = extractResource(resourceRepository.findOne(resourceId, queryAdapter));

        String attributesFromFindOne = null;
        try {
            // extract current attributes from findOne without any manipulation by query params (such as sparse fieldsets)
            attributesFromFindOne = this.extractAttributesFromResourceAsJson(resource, jsonPath, new QueryParamsAdapter(new QueryParams()));
            Map<String,Object> attributesToUpdate = objectMapper.readValue(attributesFromFindOne, Map.class);
            // deserialize the request JSON's attributes object into a map
            String attributesAsJson = objectMapper.writeValueAsString(dataBody.getAttributes());
            Map<String,Object> attributesFromRequest = objectMapper.readValue(attributesAsJson, Map.class);;

            // remove attributes that were omitted in the request
            Iterator<String> it = attributesToUpdate.keySet().iterator();
            while(it.hasNext()) {
                String key = it.next();
                if(!attributesFromRequest.containsKey(key))
                    it.remove();
            }

            // walk the source map and apply target values from request
            updateValues(attributesToUpdate, attributesFromRequest);
            JsonNode upsertedAttributes = objectMapper.valueToTree(attributesToUpdate);
            dataBody.setAttributes(upsertedAttributes);
        } catch (Exception e) {
            attributesFromFindOne = "";
        }

        setAttributes(dataBody, resource, bodyRegistryEntry.getResourceInformation());
        setRelations(resource, bodyRegistryEntry, dataBody, queryAdapter, parameterProvider);
        JsonApiResponse response = resourceRepository.update(resource, queryAdapter);

        return new ResourceResponseContext(response, jsonPath, queryAdapter);
    }

    private String extractAttributesFromResourceAsJson(Object resource, JsonPath jsonPath, QueryAdapter queryAdapter) throws Exception {

        JsonApiResponse response = new JsonApiResponse();
        response.setEntity(resource);
        ResourceResponseContext katharsisResponse = new ResourceResponseContext(response, jsonPath, queryAdapter);
        // deserialize using the objectMapper so it becomes json-api
        String newRequestBody = objectMapper.writeValueAsString(katharsisResponse);
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
