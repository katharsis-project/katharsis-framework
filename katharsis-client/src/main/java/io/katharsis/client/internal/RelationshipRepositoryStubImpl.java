package io.katharsis.client.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;

import io.katharsis.client.KatharsisClient;
import io.katharsis.client.QuerySpecRelationshipRepositoryStub;
import io.katharsis.client.RelationshipRepositoryStub;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.CollectionResponseContext;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.LinkageContainer;
import io.katharsis.response.ResourceResponseContext;
import io.katharsis.utils.PropertyUtils;

public class RelationshipRepositoryStubImpl<T, TID extends Serializable, D, DID extends Serializable> extends AbstractStub
		implements RelationshipRepositoryStub<T, TID, D, DID>, QuerySpecRelationshipRepositoryStub<T, TID, D, DID> {

	private Class<T> sourceClass;

	private Class<D> targetClass;

	private ResourceInformation resourceInformation;

	private RegistryEntry<?> relationshipEntry;

	public RelationshipRepositoryStubImpl(KatharsisClient client, Class<T> sourceClass, Class<D> targetClass,
			ResourceInformation resourceInformation, RequestUrlBuilder urlBuilder, RegistryEntry<?> relationshipEntry) {
		super(client, urlBuilder);
		this.sourceClass = sourceClass;
		this.targetClass = targetClass;
		this.resourceInformation = resourceInformation;
		this.relationshipEntry = relationshipEntry;
	}

	@Override
	public void setRelation(T source, DID targetId, String fieldName) {
		Serializable sourceId = getSourceId(source);
		HttpUrl url = urlBuilder.buildUrl(sourceClass, sourceId, (QuerySpec) null, fieldName);
		execute(url, "PATCH", targetId);
	}

	@Override
	public void setRelations(T source, Iterable<DID> targetIds, String fieldName) {
		Serializable sourceId = getSourceId(source);
		HttpUrl url = urlBuilder.buildUrl(sourceClass, sourceId, (QuerySpec) null, fieldName);
		execute(url, "PATCH", targetIds);
	}

	@Override
	public void addRelations(T source, Iterable<DID> targetIds, String fieldName) {
		Serializable sourceId = getSourceId(source);
		HttpUrl url = urlBuilder.buildUrl(sourceClass, sourceId, (QuerySpec) null, fieldName);
		execute(url, "POST", targetIds);
	}

	@Override
	public void removeRelations(T source, Iterable<DID> targetIds, String fieldName) {
		Serializable sourceId = getSourceId(source);
		HttpUrl url = urlBuilder.buildUrl(sourceClass, sourceId, (QuerySpec) null, fieldName);
		execute(url, "DELETE", targetIds);
	}

	private Serializable getSourceId(T source) {
		ResourceField idField = resourceInformation.getIdField();
		return (Serializable) PropertyUtils.getProperty(source, idField.getUnderlyingName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public D findOneTarget(TID sourceId, String fieldName, QueryParams queryParams) {
		HttpUrl url = urlBuilder.buildUrl(sourceClass, sourceId, queryParams, fieldName);
		BaseResponseContext responseContext = executeGet(url);
		return (D) responseContext.getResponse().getEntity();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<D> findManyTargets(TID sourceId, String fieldName, QueryParams queryParams) {
		HttpUrl url = urlBuilder.buildUrl(sourceClass, sourceId, queryParams, fieldName);
		BaseResponseContext responseContext = executeGet(url);
		return (List<D>) responseContext.getResponse().getEntity();
	}

	@Override
	public D findOneTarget(TID sourceId, String fieldName, QuerySpec querySpec) {
		HttpUrl url = urlBuilder.buildUrl(sourceClass, sourceId, querySpec, fieldName);
		BaseResponseContext responseContext = executeGet(url);
		return (D) responseContext.getResponse().getEntity();
	}

	@Override
	public List<D> findManyTargets(TID sourceId, String fieldName, QuerySpec querySpec) {
		HttpUrl url = urlBuilder.buildUrl(sourceClass, sourceId, querySpec, fieldName);
		BaseResponseContext responseContext = executeGet(url);
		return (List<D>) responseContext.getResponse().getEntity();
	}

	private void execute(HttpUrl requestUrl, String method, Object targetIds) {
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

		Builder builder = new Request.Builder().url(requestUrl);
		builder = builder.method(method, RequestBody.create(null, requestBodyValue));
		execute(builder, false);
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
