package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.dispatcher.controller.Utils;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.Request;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.ResourceRepositoryAdapter;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.ResourceResponseContext;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;
import java.util.Map;

public class ResourcePatch extends ResourceUpsert {

    public ResourcePatch(ResourceRegistry resourceRegistry,
                         TypeParser typeParser,
                         QueryParamsBuilder paramsBuilder,
                         ObjectMapper objectMapper) {
        super(resourceRegistry, typeParser, paramsBuilder, objectMapper);
    }

    @Override
    public boolean isAcceptable(Request request) {
        return request.getMethod() == HttpMethod.PATCH
                && request.getPath().isResource()
                && !request.getPath().isCollection();
    }

    @Override
    public BaseResponseContext handle(Request request) {
        JsonApiPath path = request.getPath();

        RegistryEntry endpointRegistryEntry = resourceRegistry.getEntry(path.getResource());
        Utils.checkResourceExists(endpointRegistryEntry, path.getResource());
        DataBody dataBody = dataBody(request);

        RegistryEntry bodyRegistryEntry = resourceRegistry.getEntry(dataBody.getType());
        verifyTypes(HttpMethod.PATCH, path.getResource(), endpointRegistryEntry, bodyRegistryEntry);

        String idString = path.getIds().get().get(0);
        Serializable resourceId = parseId(endpointRegistryEntry, idString);

        ResourceRepositoryAdapter resourceRepository = endpointRegistryEntry.getResourceRepository(request.getParameterProvider());

        QueryParams queryParams = getQueryParamsBuilder().parseQuery(path.getQuery());

        @SuppressWarnings("unchecked")
        Object resource = extractResource(resourceRepository.findOne(resourceId, queryParams));

        String attributesFromFindOne = null;
        try {
            // extract attributes from find one without any manipulation by query params (such as sparse fieldsets)
            attributesFromFindOne = this.extractAttributesFromResourceAsJson(resource, request.getPath(), new QueryParams());
            Map<String,Object> attributesToUpdate = objectMapper.readValue(attributesFromFindOne, Map.class);
            // get the JSON form the request and deserialize into a map
            String attributesAsJson = objectMapper.writeValueAsString(dataBody.getAttributes());
            Map<String,Object> attributesFromRequest = objectMapper.readValue(attributesAsJson, Map.class);
            // walk the source map and apply target values from request
            updateValues(attributesToUpdate, attributesFromRequest);
            JsonNode upsertedAttributes = objectMapper.valueToTree(attributesToUpdate);
            dataBody.setAttributes(upsertedAttributes);
        } catch (Exception e) {
            attributesFromFindOne = "";
        }

        setAttributes(dataBody, resource, bodyRegistryEntry.getResourceInformation());
        setRelations(resource, bodyRegistryEntry, dataBody, queryParams, request.getParameterProvider());
        JsonApiResponse response = resourceRepository.save(resource, queryParams);

        return new ResourceResponseContext(response, path, queryParams);
    }

    private String extractAttributesFromResourceAsJson(Object resource, JsonApiPath jsonPath, QueryParams queryParams) throws Exception {

        JsonApiResponse response = new JsonApiResponse();
        response.setEntity(resource);
        ResourceResponseContext katharsisResponse = new ResourceResponseContext(response, jsonPath, queryParams);
        // deserialize using the objectMapper so it becomes json-api
        String newRequestBody = objectMapper.writeValueAsString(katharsisResponse);
        JsonNode node = objectMapper.readTree(newRequestBody);
        JsonNode attributes = node.findValue("attributes");
        return objectMapper.writeValueAsString(attributes);

    }

    private void updateValues(Map<String, Object> source,
                    Map<String, Object> updates) {

        for (Map.Entry<String, Object> entry : source.entrySet()) {
            if (!updates.containsKey(entry.getKey())) {
                continue;
            }
            Object obj = entry.getValue();
            Object upd = updates.get(entry.getKey());
            if (obj instanceof Map) {
                updateValues((Map<String, Object>)obj, (Map<String, Object>)upd);
                continue;
            }
            source.put(entry.getKey(), upd);
        }

    }

}
