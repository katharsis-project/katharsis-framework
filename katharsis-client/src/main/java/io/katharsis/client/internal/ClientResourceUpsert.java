package io.katharsis.client.internal;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.katharsis.client.ResponseBodyException;
import io.katharsis.client.internal.proxy.ClientProxyFactory;
import io.katharsis.dispatcher.controller.Response;
import io.katharsis.dispatcher.controller.resource.ResourceUpsert;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.path.JsonPath;
import io.katharsis.resource.Document;
import io.katharsis.resource.Relationship;
import io.katharsis.resource.Resource;
import io.katharsis.resource.ResourceIdentifier;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.internal.DocumentMapper;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.PropertyUtils;
import io.katharsis.utils.parser.TypeParser;

class ClientResourceUpsert extends ResourceUpsert {

	private ClientProxyFactory proxyFactory;

	private Map<String, Object> resourceMap = new HashMap<>();

	public ClientResourceUpsert(ResourceRegistry resourceRegistry, TypeParser typeParser, ObjectMapper objectMapper, DocumentMapper documentMapper, ClientProxyFactory proxyFactory) {
		super(resourceRegistry, typeParser, objectMapper, documentMapper);
		this.proxyFactory = proxyFactory;
	}

	public String getUID(ResourceIdentifier id) {
		return id.getType() + "#" + id.getId();
	}

	public String getUID(RegistryEntry<?> entry, Serializable id) {
		return entry.getResourceInformation().getResourceType() + "#" + id;
	}

	public void setRelations(List<Resource> resources) {
		for (Resource resource : resources) {
			String uid = getUID(resource);
			Object object = resourceMap.get(uid);

			RegistryEntry<?> registryEntry = resourceRegistry.getEntry(resource.getType());
			QueryAdapter queryAdapter = null;
			RepositoryMethodParameterProvider parameterProvider = null;

			setRelations(object, registryEntry, resource, queryAdapter, parameterProvider);
		}
	}

	/**
	 * Get relations from includes section or create a remote proxy
	 */
	@Override
	protected Object fetchRelatedObject(RegistryEntry entry, Serializable relationId, RepositoryMethodParameterProvider parameterProvider, QueryAdapter queryAdapter) {

		String uid = getUID(entry, relationId);
		Object relatedResource = resourceMap.get(uid);
		if (relatedResource != null) {
			return relatedResource;
		} else {
			ResourceInformation resourceInformation = entry.getResourceInformation();
			Class<?> resourceClass = resourceInformation.getResourceClass();
			String url = null;
			return proxyFactory.createResourceProxy(resourceClass, relationId, url);
		}
	}

	public List<Object> allocateResources(List<Resource> resources) {
		List<Object> objects = new ArrayList<>();
		for (Resource resource : resources) {

			RegistryEntry<?> registryEntry = resourceRegistry.getEntry(resource.getType());
			ResourceInformation resourceInformation = registryEntry.getResourceInformation();

			Object object = newResource(resourceInformation, resource);
			setId(resource, object, resourceInformation);
			setAttributes(resource, object, resourceInformation);
			setLinks(resource, object, resourceInformation);
			setMeta(resource, object, resourceInformation);

			objects.add(object);

			String uid = getUID(resource);
			resourceMap.put(uid, object);
		}
		return objects;
	}

	protected void setLinks(Resource dataBody, Object instance, ResourceInformation resourceInformation) {
		String linksFieldName = resourceInformation.getLinksFieldName();
		if (dataBody.getLinks() != null && linksFieldName != null) {
			JsonNode linksNode = dataBody.getLinks();
			Class<?> linksClass = PropertyUtils.getPropertyClass(resourceInformation.getResourceClass(), linksFieldName);
			ObjectReader linksMapper = objectMapper.readerFor(linksClass);
			try {
				Object links = linksMapper.readValue(linksNode);
				PropertyUtils.setProperty(instance, linksFieldName, links);
			} catch (IOException e) {
				throw new ResponseBodyException("failed to parse links information", e);
			}
		}
	}

	protected void setMeta(Resource dataBody, Object instance, ResourceInformation resourceInformation) {
		String metaFieldName = resourceInformation.getMetaFieldName();
		if (dataBody.getMeta() != null && metaFieldName != null) {
			JsonNode metaNode = dataBody.getMeta();

			Class<?> metaClass = PropertyUtils.getPropertyClass(resourceInformation.getResourceClass(), metaFieldName);

			ObjectReader metaMapper = objectMapper.readerFor(metaClass);
			try {
				Object meta = metaMapper.readValue(metaNode);
				PropertyUtils.setProperty(instance, metaFieldName, meta);
			} catch (IOException e) {
				throw new ResponseBodyException("failed to parse links information", e);
			}

		}
	}

	@Override
	public boolean isAcceptable(JsonPath jsonPath, String requestType) {
		throw new IllegalStateException();
	}

	@Override
	public Response handle(JsonPath jsonPath, QueryAdapter queryAdapter, RepositoryMethodParameterProvider parameterProvider, Document document) {
		throw new IllegalStateException();
	}

	@Override
	protected void setRelationsField(Object newResource, RegistryEntry registryEntry, Map.Entry<String, Relationship> property, QueryAdapter queryAdapter, RepositoryMethodParameterProvider parameterProvider) {

		Relationship relationship = property.getValue();

		if (!relationship.getData().isPresent()) {
			ObjectNode links = relationship.getLinks();
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
		} else {
			// set elements
			super.setRelationsField(newResource, registryEntry, property, queryAdapter, parameterProvider);
		}
	}

}
