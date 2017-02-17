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
import io.katharsis.core.internal.dispatcher.controller.ResourceUpsert;
import io.katharsis.core.internal.dispatcher.path.JsonPath;
import io.katharsis.core.internal.resource.DocumentMapper;
import io.katharsis.core.internal.utils.PropertyUtils;
import io.katharsis.errorhandling.exception.RepositoryNotFoundException;
import io.katharsis.legacy.internal.RepositoryMethodParameterProvider;
import io.katharsis.repository.request.QueryAdapter;
import io.katharsis.repository.response.Response;
import io.katharsis.resource.Document;
import io.katharsis.resource.Relationship;
import io.katharsis.resource.Resource;
import io.katharsis.resource.ResourceIdentifier;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
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

	public String getUID(RegistryEntry entry, Serializable id) {
		return entry.getResourceInformation().getResourceType() + "#" + id;
	}

	public void setRelations(List<Resource> resources) {
		for (Resource resource : resources) {
			String uid = getUID(resource);
			Object object = resourceMap.get(uid);

			RegistryEntry registryEntry = resourceRegistry.getEntry(resource.getType());

			// no need for any query parameters when doing POST/PATCH
			QueryAdapter queryAdapter = null;

			// no in use on the client side
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
		}
		ResourceInformation resourceInformation = entry.getResourceInformation();
		Class<?> resourceClass = resourceInformation.getResourceClass();
		return proxyFactory.createResourceProxy(resourceClass, relationId);
	}

	public List<Object> allocateResources(List<Resource> resources) {
		List<Object> objects = new ArrayList<>();
		for (Resource resource : resources) {

			RegistryEntry registryEntry = resourceRegistry.getEntry(resource.getType());
			if (registryEntry == null) {
				throw new RepositoryNotFoundException(resource.getType());
			}
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
		ResourceField linksField = resourceInformation.getLinksField();
		if (dataBody.getLinks() != null && linksField != null) {
			JsonNode linksNode = dataBody.getLinks();
			Class<?> linksClass = linksField.getType();
			ObjectReader linksMapper = objectMapper.readerFor(linksClass);
			try {
				Object links = linksMapper.readValue(linksNode);
				PropertyUtils.setProperty(instance, linksField.getUnderlyingName(), links);
			} catch (IOException e) {
				throw new ResponseBodyException("failed to parse links information", e);
			}
		}
	}

	protected void setMeta(Resource dataBody, Object instance, ResourceInformation resourceInformation) {
		ResourceField metaField = resourceInformation.getMetaField();
		if (dataBody.getMeta() != null && metaField != null) {
			JsonNode metaNode = dataBody.getMeta();

			Class<?> metaClass = metaField.getType();

			ObjectReader metaMapper = objectMapper.readerFor(metaClass);
			try {
				Object meta = metaMapper.readValue(metaNode);
				PropertyUtils.setProperty(instance, metaField.getUnderlyingName(), meta);
			} catch (IOException e) {
				throw new ResponseBodyException("failed to parse links information", e);
			}

		}
	}

	@Override
	public boolean isAcceptable(JsonPath jsonPath, String requestType) {
		// no in use on client side, consider refactoring ResourceUpsert to
		// separate from controllers
		throw new UnsupportedOperationException();
	}

	@Override
	public Response handle(JsonPath jsonPath, QueryAdapter queryAdapter, RepositoryMethodParameterProvider parameterProvider, Document document) {
		// no in use on client side, consider refactoring ResourceUpsert to
		// separate from controllers
		throw new UnsupportedOperationException();
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
