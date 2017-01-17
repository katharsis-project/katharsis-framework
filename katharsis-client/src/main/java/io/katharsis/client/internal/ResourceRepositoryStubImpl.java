package io.katharsis.client.internal;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.client.KatharsisClient;
import io.katharsis.client.QuerySpecResourceRepositoryStub;
import io.katharsis.client.ResourceRepositoryStub;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.resource.Document;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.list.DefaultResourceList;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.utils.JsonApiUrlBuilder;
import io.katharsis.utils.PropertyUtils;

public class ResourceRepositoryStubImpl<T, I extends Serializable> extends AbstractStub implements ResourceRepositoryStub<T, I>, QuerySpecResourceRepositoryStub<T, I> {

	private ResourceInformation resourceInformation;

	private Class<T> resourceClass;

	public ResourceRepositoryStubImpl(KatharsisClient client, Class<T> resourceClass, ResourceInformation resourceInformation, JsonApiUrlBuilder urlBuilder) {
		super(client, urlBuilder);
		this.resourceClass = resourceClass;
		this.resourceInformation = resourceInformation;
	}

	private Object executeUpdate(String requestUrl, T resource, boolean create) {
		JsonApiResponse response = new JsonApiResponse();
		response.setEntity(resource);

		ClientDocumentMapper documentMapper = client.getDocumentMapper();
		Document requestDocument = documentMapper.toDocument(response, null);

		ObjectMapper objectMapper = client.getObjectMapper();
		String requestBodyValue;
		try {
			requestBodyValue = objectMapper.writeValueAsString(requestDocument);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException(e);
		}

		HttpMethod method = create || client.getPushAlways() ? HttpMethod.POST : HttpMethod.PATCH;

		return execute(requestUrl, ResponseType.RESOURCE, method, requestBodyValue);
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
		Object id = getId(entity, create);
		String url = urlBuilder.buildUrl(resourceClass, id, (QuerySpec) null);
		return (S) executeUpdate(url, entity, create);
	}

	@Override
	public <S extends T> S create(S entity) {
		return modify(entity, true);
	}

	private <S extends T> Object getId(S entity, boolean create) {
		if (client.getPushAlways()) {
			return null;
		}
		if (create) {
			return null;
		} else {
			ResourceField idField = resourceInformation.getIdField();
			return PropertyUtils.getProperty(entity, idField.getUnderlyingName());
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

	@SuppressWarnings("unchecked")
	public DefaultResourceList<T> findAll(String url) {
		return (DefaultResourceList<T>) executeGet(url, ResponseType.RESOURCES);
	}

	@SuppressWarnings("unchecked")
	private T findOne(String url) {
		return (T) executeGet(url, ResponseType.RESOURCE);
	}

}
