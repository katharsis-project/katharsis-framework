package io.katharsis.client.internal;

import java.io.Serializable;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.client.KatharsisClient;
import io.katharsis.client.QuerySpecRelationshipRepositoryStub;
import io.katharsis.client.RelationshipRepositoryStub;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.list.DefaultResourceList;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.CollectionResponseContext;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.LinkageContainer;
import io.katharsis.response.ResourceResponseContext;
import io.katharsis.utils.JsonApiUrlBuilder;
import io.katharsis.utils.PropertyUtils;

public class RelationshipRepositoryStubImpl<T, I extends Serializable, D, J extends Serializable> extends AbstractStub
		implements RelationshipRepositoryStub<T, I, D, J>, QuerySpecRelationshipRepositoryStub<T, I, D, J> {

	private Class<T> sourceClass;

	private Class<D> targetClass;

	private ResourceInformation resourceInformation;

	private RegistryEntry<?> relationshipEntry;

	public RelationshipRepositoryStubImpl(KatharsisClient client, Class<T> sourceClass, Class<D> targetClass,
			ResourceInformation resourceInformation, JsonApiUrlBuilder urlBuilder, RegistryEntry<?> relationshipEntry) {
		super(client, urlBuilder);
		this.sourceClass = sourceClass;
		this.targetClass = targetClass;
		this.resourceInformation = resourceInformation;
		this.relationshipEntry = relationshipEntry;
	}

	@Override
	public void setRelation(T source, J targetId, String fieldName) {
		Serializable sourceId = getSourceId(source);
		String url = urlBuilder.buildUrl(sourceClass, sourceId, (QuerySpec) null, fieldName);
		execute(url, HttpMethod.PATCH, targetId);
	}

	@Override
	public void setRelations(T source, Iterable<J> targetIds, String fieldName) {
		Serializable sourceId = getSourceId(source);
		String url = urlBuilder.buildUrl(sourceClass, sourceId, (QuerySpec) null, fieldName);
		execute(url, HttpMethod.PATCH, targetIds);
	}

	@Override
	public void addRelations(T source, Iterable<J> targetIds, String fieldName) {
		Serializable sourceId = getSourceId(source);
		String url = urlBuilder.buildUrl(sourceClass, sourceId, (QuerySpec) null, fieldName);
		execute(url, HttpMethod.POST, targetIds);
	}

	@Override
	public void removeRelations(T source, Iterable<J> targetIds, String fieldName) {
		Serializable sourceId = getSourceId(source);
		String url = urlBuilder.buildUrl(sourceClass, sourceId, (QuerySpec) null, fieldName);
		execute(url, HttpMethod.DELETE, targetIds);
	}

	private Serializable getSourceId(T source) {
		ResourceField idField = resourceInformation.getIdField();
		return (Serializable) PropertyUtils.getProperty(source, idField.getUnderlyingName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public D findOneTarget(I sourceId, String fieldName, QueryParams queryParams) {
		String url = urlBuilder.buildUrl(sourceClass, sourceId, queryParams, fieldName);
		BaseResponseContext responseContext = executeGet(url);
		return (D) responseContext.getResponse().getEntity();
	}

	@Override
	public DefaultResourceList<D> findManyTargets(I sourceId, String fieldName, QueryParams queryParams) {
		String url = urlBuilder.buildUrl(sourceClass, sourceId, queryParams, fieldName);
		BaseResponseContext responseContext = executeGet(url);
		return toList(responseContext.getResponse());
	}

	@SuppressWarnings("unchecked")
	@Override
	public D findOneTarget(I sourceId, String fieldName, QuerySpec querySpec) {
		String url = urlBuilder.buildUrl(sourceClass, sourceId, querySpec, fieldName);
		BaseResponseContext responseContext = executeGet(url);
		return (D) responseContext.getResponse().getEntity();
	}

	@Override
	public DefaultResourceList<D> findManyTargets(I sourceId, String fieldName, QuerySpec querySpec) {
		String url = urlBuilder.buildUrl(sourceClass, sourceId, querySpec, fieldName);
		BaseResponseContext responseContext = executeGet(url);
		return toList(responseContext.getResponse());
	}

	private void execute(String requestUrl, HttpMethod method, Object targetIds) {
		JsonPath fieldPath = new ResourcePath(resourceInformation.getResourceType());

		JsonApiResponse response = new JsonApiResponse();
		BaseResponseContext context;
		if (targetIds instanceof Iterable) {
			ArrayList<LinkageContainer> containers = new ArrayList<>();
			for (Object targetId : (Iterable<?>) targetIds) {
				containers.add(new LinkageContainer(targetId, targetClass, relationshipEntry));
			}
			response.setEntity(containers);
			context = new CollectionResponseContext(response, fieldPath, null);
		}
		else {
			Object targetId = targetIds;
			response.setEntity(new LinkageContainer(targetId, targetClass, relationshipEntry));
			context = new ResourceResponseContext(response, fieldPath, (QueryAdapter) null);
		}

		ObjectMapper objectMapper = katharsis.getObjectMapper();
		String requestBodyValue;
		try {
			requestBodyValue = objectMapper.writeValueAsString(context);
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException(e);
		}

		execute(requestUrl, false, method, requestBodyValue);
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
