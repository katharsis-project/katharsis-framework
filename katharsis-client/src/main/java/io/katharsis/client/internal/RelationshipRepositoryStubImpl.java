package io.katharsis.client.internal;

import java.io.Serializable;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.client.KatharsisClient;
import io.katharsis.client.RelationshipRepositoryStub;
import io.katharsis.core.internal.utils.JsonApiUrlBuilder;
import io.katharsis.core.internal.utils.PropertyUtils;
import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.RelationshipRepositoryV2;
import io.katharsis.repository.request.HttpMethod;
import io.katharsis.resource.Document;
import io.katharsis.resource.ResourceIdentifier;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.list.DefaultResourceList;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.utils.Nullable;

public class RelationshipRepositoryStubImpl<T, I extends Serializable, D, J extends Serializable> extends AbstractStub implements RelationshipRepositoryStub<T, I, D, J>, RelationshipRepositoryV2<T, I, D, J> {

	private Class<T> sourceClass;

	private Class<D> targetClass;

	private ResourceInformation sourceResourceInformation;

	private RegistryEntry relationshipEntry;

	public RelationshipRepositoryStubImpl(KatharsisClient client, Class<T> sourceClass, Class<D> targetClass, ResourceInformation sourceResourceInformation, JsonApiUrlBuilder urlBuilder, RegistryEntry relationshipEntry) {
		super(client, urlBuilder);
		this.sourceClass = sourceClass;
		this.targetClass = targetClass;
		this.sourceResourceInformation = sourceResourceInformation;
		this.relationshipEntry = relationshipEntry;
	}

	@Override
	public void setRelation(T source, J targetId, String fieldName) {
		Serializable sourceId = getSourceId(source);
		String url = urlBuilder.buildUrl(sourceResourceInformation, sourceId, (QuerySpec) null, fieldName);
		executeWithId(url, HttpMethod.PATCH, targetId);
	}

	@Override
	public void setRelations(T source, Iterable<J> targetIds, String fieldName) {
		Serializable sourceId = getSourceId(source);
		String url = urlBuilder.buildUrl(sourceResourceInformation, sourceId, (QuerySpec) null, fieldName);
		executeWithIds(url, HttpMethod.PATCH, targetIds);
	}

	@Override
	public void addRelations(T source, Iterable<J> targetIds, String fieldName) {
		Serializable sourceId = getSourceId(source);
		String url = urlBuilder.buildUrl(sourceResourceInformation, sourceId, (QuerySpec) null, fieldName);
		executeWithIds(url, HttpMethod.POST, targetIds);
	}

	@Override
	public void removeRelations(T source, Iterable<J> targetIds, String fieldName) {
		Serializable sourceId = getSourceId(source);
		String url = urlBuilder.buildUrl(sourceResourceInformation, sourceId, (QuerySpec) null, fieldName);
		executeWithIds(url, HttpMethod.DELETE, targetIds);
	}

	private Serializable getSourceId(T source) {
		ResourceField idField = sourceResourceInformation.getIdField();
		return (Serializable) PropertyUtils.getProperty(source, idField.getUnderlyingName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public D findOneTarget(I sourceId, String fieldName, QueryParams queryParams) {
		String url = urlBuilder.buildUrl(sourceResourceInformation, sourceId, queryParams, fieldName);
		return (D) executeGet(url, ResponseType.RESOURCE);
	}

	@Override
	public DefaultResourceList<D> findManyTargets(I sourceId, String fieldName, QueryParams queryParams) {
		String url = urlBuilder.buildUrl(sourceResourceInformation, sourceId, queryParams, fieldName);
		return (DefaultResourceList<D>) executeGet(url, ResponseType.RESOURCES);
	}

	@SuppressWarnings("unchecked")
	@Override
	public D findOneTarget(I sourceId, String fieldName, QuerySpec querySpec) {
		String url = urlBuilder.buildUrl(sourceResourceInformation, sourceId, querySpec, fieldName);
		return (D) executeGet(url, ResponseType.RESOURCE);
	}

	@Override
	public DefaultResourceList<D> findManyTargets(I sourceId, String fieldName, QuerySpec querySpec) {
		String url = urlBuilder.buildUrl(sourceResourceInformation, sourceId, querySpec, fieldName);
		return (DefaultResourceList<D>) executeGet(url, ResponseType.RESOURCES);
	}

	private void executeWithIds(String requestUrl, HttpMethod method, Iterable<?> targetIds) {
		Document document = new Document();
		ArrayList<ResourceIdentifier> resourceIdentifiers = new ArrayList<>();
		for (Object targetId : (Iterable<?>) targetIds) {
			String strTargetId = sourceResourceInformation.toIdString(targetId);
			resourceIdentifiers.add(new ResourceIdentifier(strTargetId, sourceResourceInformation.getResourceType()));
		}
		document.setData(Nullable.of((Object) resourceIdentifiers));
		doExecute(requestUrl, method, document);
	}
	
	private void executeWithId(String requestUrl, HttpMethod method, Object targetIds) {
		Document document = new Document();
		String strTargetId = sourceResourceInformation.toIdString(targetIds);
		ResourceIdentifier resourceIdentifier = new ResourceIdentifier(strTargetId, sourceResourceInformation.getResourceType());
		document.setData(Nullable.of((Object) resourceIdentifier));
		doExecute(requestUrl, method, document);
	}

	private void doExecute(String requestUrl, HttpMethod method, Document document) {
		ObjectMapper objectMapper = client.getObjectMapper();
		String requestBodyValue;
		try {
			requestBodyValue = objectMapper.writeValueAsString(document);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException(e);
		}
		execute(requestUrl, ResponseType.NONE, method, requestBodyValue);
	}

	@Override
	public Class<T> getSourceResourceClass() {
		return sourceClass;
	}

	@Override
	public Class<D> getTargetResourceClass() {
		return targetClass;
	}
}
