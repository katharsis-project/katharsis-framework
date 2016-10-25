package io.katharsis.client.internal;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;

import io.katharsis.client.KatharsisClient;
import io.katharsis.client.QuerySpecResourceRepositoryStub;
import io.katharsis.client.ResourceRepositoryStub;
import io.katharsis.client.response.ResourceList;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.queryspec.internal.QuerySpecAdapter;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.CollectionResponseContext;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.ResourceResponseContext;
import io.katharsis.utils.JsonApiUrlBuilder;
import io.katharsis.utils.PropertyUtils;

public class ResourceRepositoryStubImpl<T, I extends Serializable> extends AbstractStub
		implements ResourceRepositoryStub<T, I>, QuerySpecResourceRepositoryStub<T, I> {

	private ResourceInformation resourceInformation;

	private Class<T> resourceClass;

	public ResourceRepositoryStubImpl(KatharsisClient client, Class<T> resourceClass, ResourceInformation resourceInformation,
			JsonApiUrlBuilder urlBuilder) {
		super(client, urlBuilder);
		this.resourceClass = resourceClass;
		this.resourceInformation = resourceInformation;
	}

	private BaseResponseContext executeUpdate(HttpUrl requestUrl, T resource, QueryAdapter queryAdapter, boolean create) {
		JsonApiResponse response = new JsonApiResponse();
		response.setEntity(resource);

		JsonPath jsonPath = new ResourcePath(resourceInformation.getResourceType());
		ResourceResponseContext context = new ResourceResponseContext(response, jsonPath, queryAdapter);

		ObjectMapper objectMapper = katharsis.getObjectMapper();
		String requestBodyValue;
		try {
			requestBodyValue = objectMapper.writeValueAsString(context);
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException(e);
		}

		Builder builder = new Request.Builder().url(requestUrl);

		if (create || katharsis.getPushAlways()) {
			builder = builder.post(RequestBody.create(null, requestBodyValue));
		}
		else {
			builder = builder.patch(RequestBody.create(null, requestBodyValue));
		}

		return execute(builder, true);
	}

	@Override
	public T findOne(I id, QueryParams queryParams) {
		HttpUrl url = HttpUrl.parse(urlBuilder.buildUrl(resourceClass, id, queryParams));
		return findOne(url);
	}

	@Override
	public List<T> findAll(QueryParams queryParams) {
		HttpUrl url = HttpUrl.parse(urlBuilder.buildUrl(resourceClass, null, queryParams));
		return findAll(url);
	}

	@Override
	public List<T> findAll(Iterable<I> ids, QueryParams queryParams) {
		HttpUrl url = HttpUrl.parse(urlBuilder.buildUrl(resourceClass, ids, queryParams));
		return findAll(url);
	}

	@Override
	public <S extends T> S save(S entity) {
		return save(entity, new QuerySpec(resourceClass));
	}

	@Override
	public <S extends T> S save(S entity, QueryParams queryParams) {
		return modify(entity, queryParams, false);
	}

	@SuppressWarnings("unchecked")
	private <S extends T> S modify(S entity, QueryParams queryParams, boolean create) {
		String strId = getStringId(entity, create);
		HttpUrl url = HttpUrl.parse(urlBuilder.buildUrl(resourceClass, strId, (QuerySpec) null));
		BaseResponseContext context = executeUpdate(url, entity, new QueryParamsAdapter(queryParams), create);
		return (S) context.getResponse().getEntity();
	}

	@Override
	public <S extends T> S save(S entity, QuerySpec querySpec) {
		return modify(entity, querySpec, false);
	}

	@Override
	public <S extends T> S create(S entity) {
		return modify(entity, new QuerySpec(resourceClass), true);
	}

	@Override
	public <S extends T> S create(S entity, QuerySpec querySpec) {
		return modify(entity, querySpec, true);
	}

	@Override
	public <S extends T> S create(S entity, QueryParams queryParams) {
		return modify(entity, queryParams, true);
	}

	@SuppressWarnings("unchecked")
	private <S extends T> S modify(S entity, QuerySpec querySpec, boolean create) {
		String idString = getStringId(entity, create);
		HttpUrl url = HttpUrl.parse(urlBuilder.buildUrl(resourceClass, idString, (QuerySpec) null));
		BaseResponseContext context = executeUpdate(url, entity, new QuerySpecAdapter(querySpec, katharsis.getRegistry()),
				create);
		return (S) context.getResponse().getEntity();
	}

	private <S extends T> String getStringId(S entity, boolean create) {
		if (katharsis.getPushAlways()) {
			return null;
		}
		if (create) {
			return null;
		}
		else {
			ResourceField idField = resourceInformation.getIdField();
			Object objectId = PropertyUtils.getProperty(entity, idField.getUnderlyingName());
			return resourceInformation.toIdString(objectId);
		}
	}

	@Override
	public void delete(I id) {
		HttpUrl url = HttpUrl.parse(urlBuilder.buildUrl(resourceClass, id, (QuerySpec) null));
		executeDelete(url);
	}

	@Override
	public Class<T> getResourceClass() {
		return resourceClass;
	}

	@Override
	public T findOne(I id, QuerySpec querySpec) {
		HttpUrl url = HttpUrl.parse(urlBuilder.buildUrl(resourceClass, id, querySpec));
		return findOne(url);
	}

	@Override
	public ResourceList<T> findAll(QuerySpec querySpec) {
		HttpUrl url = HttpUrl.parse(urlBuilder.buildUrl(resourceClass, null, querySpec));
		return findAll(url);
	}

	@Override
	public ResourceList<T> findAll(Iterable<I> ids, QuerySpec queryPaquerySpecrams) {
		HttpUrl url = HttpUrl.parse(urlBuilder.buildUrl(resourceClass, ids, queryPaquerySpecrams));
		return findAll(url);
	}

	private ResourceList<T> findAll(HttpUrl url) {
		BaseResponseContext responseContext = executeGet(url);
		if (responseContext instanceof CollectionResponseContext) {
			CollectionResponseContext colResponseContext = (CollectionResponseContext) responseContext;
			return toList(colResponseContext.getResponse());
		}
		else {
			return toList(responseContext.getResponse());
		}
	}

	@SuppressWarnings("unchecked")
	private T findOne(HttpUrl url) {
		BaseResponseContext responseContext = executeGet(url);
		return (T) responseContext.getResponse().getEntity();
	}

}
