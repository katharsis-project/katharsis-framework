package io.katharsis.client.internal;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;

import io.katharsis.client.KatharsisClient;
import io.katharsis.client.ResourceRepositoryStub;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.CollectionResponseContext;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.ResourceResponseContext;

public class ResourceRepositoryStubImpl<T, ID extends Serializable> extends AbstractStub
		implements ResourceRepositoryStub<T, ID> {

	private ResourceInformation resourceInformation;
	private Class<T> resourceClass;

	public ResourceRepositoryStubImpl(KatharsisClient client, Class<T> resourceClass,
			ResourceInformation resourceInformation, RequestUrlBuilder urlBuilder) {
		super(client, urlBuilder);
		this.resourceClass = resourceClass;
		this.resourceInformation = resourceInformation;
	}

	private BaseResponseContext executePost(HttpUrl requestUrl, T resource, QueryParams queryParams) {
		JsonApiResponse response = new JsonApiResponse();
		response.setEntity(resource);

		IncludeLookupSetter includeFieldSetter = new IncludeLookupSetter(katharsis.getRegistry());
		String resourceName = katharsis.getRegistry().getResourceType(resourceClass);
		includeFieldSetter.setIncludedElements(resourceName, response, queryParams, null);

		JsonPath jsonPath = new ResourcePath(resourceInformation.getResourceType());
		ResourceResponseContext context = new ResourceResponseContext(response, jsonPath, queryParams);

		ObjectMapper objectMapper = katharsis.getObjectMapper();
		String requestBodyValue;
		try {
			requestBodyValue = objectMapper.writeValueAsString(context);
		} catch (JsonProcessingException e) {
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
		BaseResponseContext responseContext = executeGet(url);
		return (T) responseContext.getResponse().getEntity();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> findAll(QueryParams queryParams) {
		HttpUrl url = urlBuilder.buildUrl(resourceClass, null, queryParams);
		CollectionResponseContext responseContext = (CollectionResponseContext) executeGet(url);
		return (List<T>) responseContext.getResponse().getEntity();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> findAll(Iterable<ID> ids, QueryParams queryParams) {
		HttpUrl url = urlBuilder.buildUrl(resourceClass, ids, queryParams);
		BaseResponseContext responseContext = executeGet(url);
		if (responseContext instanceof CollectionResponseContext) {
			CollectionResponseContext colResponseContext = (CollectionResponseContext) responseContext;
			return (List<T>) colResponseContext.getResponse().getEntity();
		} else {
			return (List<T>) Arrays.asList(responseContext.getResponse().getEntity());
		}
	}

	@Override
	public <S extends T> S save(S entity) {
		return save(entity, new QueryParams());
	}

	@Override
	public <S extends T> S save(S entity, QueryParams queryParams) {
		// String idFieldName =
		// resourceInformation.getIdField().getUnderlyingName();
		// send as post, no ids send
		// consider using patch for udpates
		Object id = null;
		// try {
		// id = PropertyUtils.getProperty(entity, idFieldName);
		// } catch (Exception e) {
		// throw new IllegalStateException(e);
		// }

		HttpUrl url = urlBuilder.buildUrl(resourceClass, id, null);
		BaseResponseContext context = executePost(url, entity, queryParams);
		return (S) context.getResponse().getEntity();
	}

	@Override
	public void delete(ID id) {
		HttpUrl url = urlBuilder.buildUrl(resourceClass, id, null);

		executeDelete(url);
	}

	// // FIXME belongs to resource information
	// @Deprecated
	// public String getResourceType(Class clazz) {
	// Class resourceClazz = ClassUtils.getJsonApiResourceClass(clazz);
	// if (resourceClazz == null) {
	// return null;
	// }
	// Annotation[] annotations = resourceClazz.getAnnotations();
	// for (Annotation annotation : annotations) {
	// if (annotation instanceof JsonApiResource) {
	// JsonApiResource apiResource = (JsonApiResource) annotation;
	// return apiResource.type();
	// }
	// }
	// // won't reach this
	// return null;
	// }
}
