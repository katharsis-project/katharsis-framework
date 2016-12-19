package io.katharsis.client.internal;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.client.KatharsisClient;
import io.katharsis.client.QuerySpecResourceRepositoryStub;
import io.katharsis.client.ResourceRepositoryStub;
import io.katharsis.client.internal.core.BaseResponseContext;
import io.katharsis.client.internal.core.CollectionResponseContext;
import io.katharsis.client.internal.core.ResourceResponseContext;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.list.DefaultResourceList;
import io.katharsis.response.JsonApiResponse;
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

	private BaseResponseContext executeUpdate(String requestUrl, T resource, boolean create) {
		JsonApiResponse response = new JsonApiResponse();
		response.setEntity(resource);

		JsonPath jsonPath = new ResourcePath(resourceInformation.getResourceType());
		ResourceResponseContext context = new ResourceResponseContext(response, jsonPath, null);

		ObjectMapper objectMapper = katharsis.getObjectMapper();
		String requestBodyValue;
		try {
			requestBodyValue = objectMapper.writeValueAsString(context);
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException(e);
		}

		HttpMethod method = create || katharsis.getPushAlways() ? HttpMethod.POST : HttpMethod.PATCH;

		return execute(requestUrl, true, method, requestBodyValue);
	}

	@Override
	public T findOne(I id, QueryParams queryParams) {
		String url = urlBuilder.buildUrl(resourceClass, id, queryParams);
		return findOne(url);
	}

	@Override
	public List<T> findAll(QueryParams queryParams) {
		String url = urlBuilder.buildUrl(resourceClass, null, queryParams);
		return findAll(url);
	}

	@Override
	public List<T> findAll(Iterable<I> ids, QueryParams queryParams) {
		String url = urlBuilder.buildUrl(resourceClass, ids, queryParams);
		return findAll(url);
	}

	@Override
	public <S extends T> S save(S entity) {
		return modify(entity, false);
	}

	@SuppressWarnings("unchecked")
	private <S extends T> S modify(S entity, boolean create) {
		String strId = getStringId(entity, create);
		String url = urlBuilder.buildUrl(resourceClass, strId, (QuerySpec) null);
		BaseResponseContext context = executeUpdate(url, entity, create);
		return (S) context.getResponse().getEntity();
	}

	@Override
	public <S extends T> S create(S entity) {
		return modify(entity, true);
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
		String url = urlBuilder.buildUrl(resourceClass, id, (QuerySpec) null);
		executeDelete(url);
	}

	@Override
	public Class<T> getResourceClass() {
		return resourceClass;
	}

	@Override
	public T findOne(I id, QuerySpec querySpec) {
		String url = urlBuilder.buildUrl(resourceClass, id, querySpec);
		return findOne(url);
	}

	@Override
	public DefaultResourceList<T> findAll(QuerySpec querySpec) {
		String url = urlBuilder.buildUrl(resourceClass, null, querySpec);
		return findAll(url);
	}

	@Override
	public DefaultResourceList<T> findAll(Iterable<I> ids, QuerySpec queryPaquerySpecrams) {
		String url = urlBuilder.buildUrl(resourceClass, ids, queryPaquerySpecrams);
		return findAll(url);
	}

	public DefaultResourceList<T> findAll(String url) {
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
	private T findOne(String url) {
		BaseResponseContext responseContext = executeGet(url);
		return (T) responseContext.getResponse().getEntity();
	}

}
