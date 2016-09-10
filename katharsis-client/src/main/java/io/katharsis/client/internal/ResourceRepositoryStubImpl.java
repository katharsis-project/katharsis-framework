package io.katharsis.client.internal;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;

import io.katharsis.client.KatharsisClient;
import io.katharsis.client.QuerySpecResourceRepositoryStub;
import io.katharsis.client.ResourceRepositoryStub;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.FilterOperator;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.queryspec.internal.QuerySpecAdapter;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.CollectionResponseContext;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.ResourceResponseContext;

public class ResourceRepositoryStubImpl<T, ID extends Serializable> extends AbstractStub
		implements ResourceRepositoryStub<T, ID>, QuerySpecResourceRepositoryStub<T, ID> {

	private ResourceInformation resourceInformation;

	private Class<T> resourceClass;

	public ResourceRepositoryStubImpl(KatharsisClient client, Class<T> resourceClass, ResourceInformation resourceInformation,
			RequestUrlBuilder urlBuilder) {
		super(client, urlBuilder);
		this.resourceClass = resourceClass;
		this.resourceInformation = resourceInformation;
	}

	private BaseResponseContext executePost(HttpUrl requestUrl, T resource, QueryAdapter queryAdapter) {
		JsonApiResponse response = new JsonApiResponse();
		response.setEntity(resource);

		IncludeLookupSetter includeFieldSetter = new IncludeLookupSetter(katharsis.getRegistry());
		String resourceName = katharsis.getRegistry().getResourceType(resourceClass);
		includeFieldSetter.setIncludedElements(resourceName, response, queryAdapter, null);

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

		builder = builder.post(RequestBody.create(null, requestBodyValue));

		return execute(builder, true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T findOne(ID id, QueryParams queryParams) {
		HttpUrl url = urlBuilder.buildUrl(resourceClass, id, queryParams);
		return findOne(url);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> findAll(QueryParams queryParams) {
		HttpUrl url = urlBuilder.buildUrl(resourceClass, null, queryParams);
		return findAll(url);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> findAll(Iterable<ID> ids, QueryParams queryParams) {
		HttpUrl url = urlBuilder.buildUrl(resourceClass, ids, queryParams);
		return findAll(url);
	}

	@Override
	public <S extends T> S save(S entity) {
		return save(entity, new QueryParams());
	}

	@Override
	public <S extends T> S save(S entity, QueryParams queryParams) {
		// TODO proper post vs patch
		Object id = null;

		HttpUrl url = urlBuilder.buildUrl(resourceClass, id, (QuerySpec) null);
		BaseResponseContext context = executePost(url, entity, new QueryParamsAdapter(queryParams));
		return (S) context.getResponse().getEntity();
	}

	@Override
	public void delete(ID id) {
		HttpUrl url = urlBuilder.buildUrl(resourceClass, id, (QuerySpec) null);

		executeDelete(url);
	}

	@Override
	public Class<T> getResourceClass() {
		return resourceClass;
	}

	@Override
	public Set<FilterOperator> getSupportedOperators() {
		throw new UnsupportedOperationException("not supported on client-side");
	}

	@Override
	public FilterOperator getDefaultOperator() {
		throw new UnsupportedOperationException("not supported on client-side");
	}

	@SuppressWarnings("unchecked")
	@Override
	public T findOne(ID id, QuerySpec querySpec) {
		HttpUrl url = urlBuilder.buildUrl(resourceClass, id, querySpec);
		return findOne(url);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> findAll(QuerySpec querySpec) {
		HttpUrl url = urlBuilder.buildUrl(resourceClass, null, querySpec);
		return findAll(url);
	}

	@Override
	public List<T> findAll(Iterable<ID> ids, QuerySpec queryPaquerySpecrams) {
		HttpUrl url = urlBuilder.buildUrl(resourceClass, ids, queryPaquerySpecrams);
		return findAll(url);
	}

	@SuppressWarnings("unchecked")
	private List<T> findAll(HttpUrl url) {
		BaseResponseContext responseContext = executeGet(url);
		if (responseContext instanceof CollectionResponseContext) {
			CollectionResponseContext colResponseContext = (CollectionResponseContext) responseContext;
			return (List<T>) colResponseContext.getResponse().getEntity();
		}
		else {
			return (List<T>) Arrays.asList(responseContext.getResponse().getEntity());
		}
	}

	private T findOne(HttpUrl url) {
		BaseResponseContext responseContext = executeGet(url);
		return (T) responseContext.getResponse().getEntity();
	}

	@Override
	public <S extends T> S save(S entity, QuerySpec querySpec) {
		// TODO proper post vs patch
		Object id = null;

		HttpUrl url = urlBuilder.buildUrl(resourceClass, id, (QuerySpec) null);
		BaseResponseContext context = executePost(url, entity, new QuerySpecAdapter(querySpec, katharsis.getRegistry()));
		return (S) context.getResponse().getEntity();
	}

}
