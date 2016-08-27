package io.katharsis.client.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.dispatcher.controller.resource.ResourceUpsert;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.CollectionResponseContext;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.ResourceResponseContext;
import io.katharsis.utils.parser.TypeParser;

/**
 * Deerializes top-level JSON object and provides ability to include compound documents
 * 
 * TODO could this be merged with RequestBodyDeserializer?
 */
public class BaseResponseDeserializer extends JsonDeserializer<BaseResponseContext> {

	private static final String INCLUDED_FIELD_NAME = "included";
	private static final String DATA_FIELD_NAME = "data";

	private ResourceRegistry resourceRegistry;
	private ObjectMapper objectMapper;
	
	private TypeParser typeParser = new TypeParser();

	public BaseResponseDeserializer(ResourceRegistry resourceRegistry, ObjectMapper objectMapper) {
		this.resourceRegistry = resourceRegistry;
		this.objectMapper = objectMapper;
	}

	@Override
	public BaseResponseContext deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

		JsonNode node = jp.readValueAsTree();
		if (node == null) {
			return null;
		}

		JsonNode data = node.get(DATA_FIELD_NAME);
		JsonNode included = node.get(INCLUDED_FIELD_NAME);
		if(data == null){
			throw new IllegalStateException("no data received");
		}
		
		ClientResourceUpsert upsert = new ClientResourceUpsert(resourceRegistry, typeParser, objectMapper);
		
		ResourceBodies dataBodies = upsert.parse(data, jp);
		ResourceBodies includedBodies = upsert.parse(included, jp);
		
		upsert.allocateResources(dataBodies);
		upsert.allocateResources(includedBodies);
		
		upsert.setRelations(dataBodies);
		upsert.setRelations(includedBodies);

		if (dataBodies.isCollection) {
			JsonApiResponse response = new JsonApiResponse();
			response.setEntity(dataBodies.resources);
			return new CollectionResponseContext(response, null, null);
		} else {
			JsonApiResponse apiResponse = new JsonApiResponse();

			if (dataBodies.resources.size() == 1) {
				apiResponse.setEntity(dataBodies.resources.get(0));
			} else {
				apiResponse.setEntity(null);
			}

			return new ResourceResponseContext(apiResponse, -1);
		}
	}
	
	class ClientResourceUpsert extends ResourceUpsert{
		
		private HashMap<Object, Object> resourceMap = new HashMap<>();

		public ClientResourceUpsert(ResourceRegistry resourceRegistry, TypeParser typeParser,
				ObjectMapper objectMapper) {
			super(resourceRegistry, typeParser, objectMapper);
		}
		
		public String getUID(DataBody body){
			return body.getType() + "#" + body.getId();
		}

		public void setRelations(ResourceBodies dataBodies) {
			for(DataBody body : dataBodies.dataBodies){
				String uid = getUID(body);
				Object resource = resourceMap.get(uid);
				
				RegistryEntry registryEntry = resourceRegistry.getEntry(body.getType());
				QueryParams queryParams = null;
				RepositoryMethodParameterProvider parameterProvider = null;
				setRelations(resource, registryEntry, body, queryParams, parameterProvider);
			}			
		}
			
		public void allocateResources(ResourceBodies dataBodies) {
			for(DataBody body : dataBodies.dataBodies){
				
				RegistryEntry<?> registryEntry = resourceRegistry.getEntry(body.getType());
				ResourceInformation resourceInformation = registryEntry.getResourceInformation();
				
				Object resource = newResource(resourceInformation, body);
				setId(body, resource, resourceInformation);
				setAttributes(body, resource, resourceInformation);
				dataBodies.resources.add(resource);
				
				String uid = getUID(body);
				resourceMap.put(uid, resource);
			}			
		}

		@Override
		public boolean isAcceptable(JsonPath jsonPath, String requestType) {
			throw new IllegalStateException();
		}

		@Override
		public BaseResponseContext handle(JsonPath jsonPath, QueryParams queryParams,
				RepositoryMethodParameterProvider parameterProvider, RequestBody requestBody) {
			throw new IllegalStateException();
		}
		
		public ResourceBodies parse(JsonNode node, JsonParser jp) throws JsonProcessingException {
			ResourceBodies bodies = new ResourceBodies();
			if (node != null) {
				if (node.isArray()) {
					Iterator<JsonNode> nodeIterator = node.iterator();
					while (nodeIterator.hasNext()) {
						DataBody newLinkage = jp.getCodec().treeToValue(nodeIterator.next(), ClientDataBody.class);
						bodies.dataBodies.add(newLinkage);
					}
					bodies.isCollection = true;
				} else if (node.isObject()) {
					bodies.dataBodies.add(jp.getCodec().treeToValue(node, ClientDataBody.class));
				} else if (node.isNull()) {
				} else {
					throw new RuntimeException("data field has wrong type: " + node.toString());
				}
			}	
			return bodies;
		}
	};
	
	public static class ClientDataBody extends DataBody{
		
		// TODO support processing of those fields
		private JsonNode links;
		private JsonNode meta;
		
		public JsonNode getLinks() {
			return links;
		}
		public void setLinks(JsonNode links) {
			this.links = links;
		}
		public JsonNode getMeta() {
			return meta;
		}
		public void setMeta(JsonNode meta) {
			this.meta = meta;
		}
	}
	
	
	class ResourceBodies{
		ArrayList<Object> resources = new ArrayList<>();
		ArrayList<DataBody> dataBodies = new ArrayList<>();
		boolean isCollection = false;
	}

}
