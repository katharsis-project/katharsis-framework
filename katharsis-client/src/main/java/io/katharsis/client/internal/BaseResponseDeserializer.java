package io.katharsis.client.internal;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import io.katharsis.client.ResponseBodyException;
import io.katharsis.client.internal.proxy.ClientProxyFactory;
import io.katharsis.client.response.JsonLinksInformation;
import io.katharsis.client.response.JsonMetaInformation;
import io.katharsis.dispatcher.controller.resource.ResourceUpsert;
import io.katharsis.jackson.exception.JsonDeserializationException;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.LinkageData;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.CollectionResponseContext;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;
import io.katharsis.response.ResourceResponseContext;
import io.katharsis.utils.PropertyUtils;
import io.katharsis.utils.parser.TypeParser;

/**
 * Deerializes top-level JSON object and provides ability to include compound documents
 * 
 * TODO could this be merged with RequestBodyDeserializer?
 */
public class BaseResponseDeserializer extends JsonDeserializer<BaseResponseContext> {

	private static final String INCLUDED_FIELD_NAME = "included";

	private static final String DATA_FIELD_NAME = "data";

	private static final String META_FIELD_NAME = "meta";

	private static final String LINKS_FIELD_NAME = "links";

	private ResourceRegistry resourceRegistry;

	private ObjectMapper objectMapper;

	private TypeParser typeParser = new TypeParser();

	private ClientProxyFactory proxyFactory;

	public BaseResponseDeserializer(ResourceRegistry resourceRegistry, ObjectMapper objectMapper) {
		this.resourceRegistry = resourceRegistry;
		this.objectMapper = objectMapper;
	}

	@Override
	public BaseResponseContext deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {

		JsonNode node = jp.readValueAsTree();
		if (node == null) {
			return null;
		}

		JsonNode data = node.get(DATA_FIELD_NAME);
		JsonNode included = node.get(INCLUDED_FIELD_NAME);
		LinksInformation links = readLinks(node);
		MetaInformation meta = readMeta(node);
		if (data == null) {
			throw new IllegalStateException("no data received");
		}

		ClientResourceUpsert upsert = new ClientResourceUpsert(resourceRegistry, typeParser, objectMapper);

		ResourceBodies dataBodies = upsert.parse(data, jp);
		ResourceBodies includedBodies = upsert.parse(included, jp);

		upsert.allocateResources(dataBodies);
		upsert.allocateResources(includedBodies);

		upsert.setRelations(dataBodies);
		upsert.setRelations(includedBodies);

		JsonApiResponse response = new JsonApiResponse();
		response.setLinksInformation(links);
		response.setMetaInformation(meta);
		if (dataBodies.isCollection) {
			response.setEntity(dataBodies.resources);
			return new CollectionResponseContext(response, null, null);
		}
		else {
			if (dataBodies.resources.size() == 1) {
				response.setEntity(dataBodies.resources.get(0));
			}
			else {
				response.setEntity(null);
			}
			return new ResourceResponseContext(response, -1);
		}
	}

	private LinksInformation readLinks(JsonNode node) {
		JsonNode data = node.get(LINKS_FIELD_NAME);
		if (data != null) {
			return new JsonLinksInformation(data, objectMapper);
		}
		else {
			return null;
		}
	}

	private MetaInformation readMeta(JsonNode node) {
		JsonNode data = node.get(META_FIELD_NAME);
		if (data != null) {
			return new JsonMetaInformation(data, objectMapper);
		}
		else {
			return null;
		}
	}

	class ClientResourceUpsert extends ResourceUpsert {

		private HashMap<Object, Object> resourceMap = new HashMap<>();

		public ClientResourceUpsert(ResourceRegistry resourceRegistry, TypeParser typeParser, ObjectMapper objectMapper) {
			super(resourceRegistry, typeParser, objectMapper);
		}

		public String getUID(DataBody body) {
			return body.getType() + "#" + body.getId();
		}

		public String getUID(RegistryEntry<?> entry, Serializable id) {
			return entry.getResourceInformation().getResourceType() + "#" + id;
		}

		public void setRelations(ResourceBodies dataBodies) {
			for (DataBody body : dataBodies.dataBodies) {
				String uid = getUID(body);
				Object resource = resourceMap.get(uid);

				RegistryEntry registryEntry = resourceRegistry.getEntry(body.getType());
				QueryAdapter queryAdapter = null;
				RepositoryMethodParameterProvider parameterProvider = null;

				setRelations(resource, registryEntry, body, queryAdapter, parameterProvider);
			}
		}

		/**
		 * Get relations from includes section or create a remote proxy
		 */
		@Override
		protected Object fetchRelatedObject(RegistryEntry entry, Serializable relationId,
				RepositoryMethodParameterProvider parameterProvider, QueryAdapter queryAdapter) {

			String uid = getUID(entry, relationId);
			Object relatedResource = resourceMap.get(uid);
			if (relatedResource != null) {
				return relatedResource;
			}
			else {
				ResourceInformation resourceInformation = entry.getResourceInformation();
				Class<?> resourceClass = resourceInformation.getResourceClass();
				String url = null;
				return proxyFactory.createResourceProxy(resourceClass, relationId, url);
			}
		}

		public void allocateResources(ResourceBodies dataBodies) {
			for (DataBody body : dataBodies.dataBodies) {
				ClientDataBody clientBody = (ClientDataBody) body;

				RegistryEntry<?> registryEntry = resourceRegistry.getEntry(body.getType());
				ResourceInformation resourceInformation = registryEntry.getResourceInformation();

				Object resource = newResource(resourceInformation, body);
				setId(body, resource, resourceInformation);
				setAttributes(body, resource, resourceInformation);
				setLinks(clientBody, resource, resourceInformation);
				setMeta(clientBody, resource, resourceInformation);

				dataBodies.resources.add(resource);

				String uid = getUID(body);
				resourceMap.put(uid, resource);
			}
		}

		protected void setLinks(ClientDataBody dataBody, Object instance, ResourceInformation resourceInformation) {
			String linksFieldName = resourceInformation.getLinksFieldName();
			if (dataBody.getLinks() != null && linksFieldName != null) {
				JsonNode linksNode = dataBody.getLinks();
				Class<?> linksClass = PropertyUtils.getPropertyClass(resourceInformation.getResourceClass(), linksFieldName);
				ObjectReader linksMapper = objectMapper.readerFor(linksClass);
				try {
					Object links = linksMapper.readValue(linksNode);
					PropertyUtils.setProperty(instance, linksFieldName, links);
				}
				catch (IOException e) {
					throw new ResponseBodyException("failed to parse links information", e);
				}
			}
		}

		protected void setMeta(ClientDataBody dataBody, Object instance, ResourceInformation resourceInformation) {
			String metaFieldName = resourceInformation.getMetaFieldName();
			if (dataBody.getMeta() != null && metaFieldName != null) {
				JsonNode metaNode = dataBody.getMeta();

				Class<?> metaClass = PropertyUtils.getPropertyClass(resourceInformation.getResourceClass(), metaFieldName);

				ObjectReader metaMapper = objectMapper.readerFor(metaClass);
				try {
					Object meta = metaMapper.readValue(metaNode);
					PropertyUtils.setProperty(instance, metaFieldName, meta);
				}
				catch (IOException e) {
					throw new ResponseBodyException("failed to parse links information", e);
				}

			}
		}

		@Override
		public boolean isAcceptable(JsonPath jsonPath, String requestType) {
			throw new IllegalStateException();
		}

		@Override
		public BaseResponseContext handle(JsonPath jsonPath, QueryAdapter queryAdapter,
				RepositoryMethodParameterProvider parameterProvider, RequestBody requestBody) {
			throw new IllegalStateException();
		}

		public ResourceBodies parse(JsonNode node, JsonParser jp) throws JsonProcessingException {
			ResourceBodies bodies = new ResourceBodies();
			if (node != null) {
				if (node.isArray()) {
					Iterator<JsonNode> nodeIterator = node.iterator();
					while (nodeIterator.hasNext()) {
						JsonNode next = nodeIterator.next();
						DataBody newLinkage = jp.getCodec().treeToValue(next, ClientDataBody.class);
						bodies.dataBodies.add(newLinkage);
					}
					bodies.isCollection = true;
				}
				else if (node.isObject()) {
					bodies.dataBodies.add(jp.getCodec().treeToValue(node, ClientDataBody.class));
				}
				else if (!node.isNull()) {
					throw new JsonDeserializationException("data field has wrong type: " + node.toString());
				}
			}
			return bodies;
		}

		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		protected void setRelationsField(Object newResource, RegistryEntry registryEntry,
				Map.Entry<String, Iterable<LinkageData>> property, QueryAdapter queryAdapter,
				RepositoryMethodParameterProvider parameterProvider, JsonNode links) {
			if (property.getValue() == null) {
				if (links != null) {
					// create proxy to lazy load relations
					String fieldName = property.getKey();
					ResourceInformation resourceInformation = registryEntry.getResourceInformation();
					ResourceField field = resourceInformation.findRelationshipFieldByName(fieldName);
					Class elementType = field.getElementType();
					Class collectionClass = field.getType();

					JsonNode relatedNode = links.get("related");
					if (relatedNode != null) {
						String url = relatedNode.asText().trim();
						Object proxy = proxyFactory.createCollectionProxy(elementType, collectionClass, url);
						PropertyUtils.setProperty(newResource, fieldName, proxy);
					}
				}
			}
			else {
				// set elements
				super.setRelationsField(newResource, registryEntry, property, queryAdapter, parameterProvider, links);
			}
		}

	}

	public static class ClientDataBody extends DataBody {

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

	class ResourceBodies {

		ArrayList<Object> resources = new ArrayList<>();

		ArrayList<DataBody> dataBodies = new ArrayList<>();

		boolean isCollection = false;
	}

	public void setProxyFactory(ClientProxyFactory proxyFactory) {
		this.proxyFactory = proxyFactory;
	}

}
