package io.katharsis.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import io.katharsis.queryParams.DefaultQueryParamsSerializer;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsSerializer;
import io.katharsis.queryspec.DefaultQuerySpecSerializer;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecSerializer;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.queryspec.internal.QuerySpecAdapter;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;

public class JsonApiUrlBuilder {

	private QueryParamsSerializer queryParamsSerializer = new DefaultQueryParamsSerializer();

	private QuerySpecSerializer querySpecSerializer;

	private ResourceRegistry resourceRegistry;

	public JsonApiUrlBuilder(ResourceRegistry resourceRegistry) {
		this.resourceRegistry = resourceRegistry;
		this.querySpecSerializer = new DefaultQuerySpecSerializer(resourceRegistry);
	}

	public <T> String buildUrl(Class<T> resourceClass, Object id, QueryParams queryParams) {
		return buildUrl(resourceClass, id, queryParams, null);
	}

	public <T> String buildUrl(Class<T> resourceClass, Object id, QuerySpec querySpec) {
		return buildUrl(resourceClass, id, querySpec, null);
	}

	public <T> String buildUrl(Class<T> resourceClass, Object id, QueryAdapter queryAdapter, String relationshipName) {
		if(queryAdapter instanceof QuerySpecAdapter){
			return buildUrl(resourceClass, id, ((QuerySpecAdapter)queryAdapter).getQuerySpec(), relationshipName);
		}else{
			return buildUrl(resourceClass, id, ((QueryParamsAdapter)queryAdapter).getQueryParams(), relationshipName);
		}
	}
	
	public <T> String buildUrl(Class<T> resourceClass, Object id, QuerySpec querySpec, String relationshipName) {
		return buildUrlInternal(resourceClass, id, querySpec, relationshipName);
	}

	public <T> String buildUrl(Class<T> resourceClass, Object id, QueryParams queryParams, String relationshipName) {
		return buildUrlInternal(resourceClass, id, queryParams, relationshipName);
	}

	private <T> String buildUrlInternal(Class<T> resourceClass, Object id, Object query, String relationshipName) {
		String url = resourceRegistry.getResourceUrl(resourceClass);
		if (!url.endsWith("/")) {
			url += "/";
		}

		RegistryEntry<?> entry = resourceRegistry.getEntry(resourceClass);
		ResourceInformation resourceInformation = entry.getResourceInformation();

		if (id instanceof Collection) {
			Collection<?> ids = (Collection<?>) id;
			Collection<String> strIds = new ArrayList<>();
			for (Object idElem : ids) {
				String strIdElem = resourceInformation.toIdString(idElem);
				strIds.add(strIdElem);
			}
			url += StringUtils.join(",", strIds) + "/";
		}
		else if (id != null) {
			String strId = resourceInformation.toIdString(id);
			url += strId + "/";
		}
		if (relationshipName != null) {
			url += "relationships/" + relationshipName + "/";
		}

		UrlParameterBuilder urlBuilder = new UrlParameterBuilder(url);
		if (query instanceof QuerySpec) {
			QuerySpec querySpec = (QuerySpec) query;
			urlBuilder.addQueryParameters(querySpecSerializer.serialize(querySpec));
		}
		else if (query instanceof QueryParams) {
			QueryParams queryParams = (QueryParams) query;
			urlBuilder.addQueryParameters(queryParamsSerializer.serializeFilters(queryParams));
			urlBuilder.addQueryParameters(queryParamsSerializer.serializeSorting(queryParams));
			urlBuilder.addQueryParameters(queryParamsSerializer.serializeGrouping(queryParams));
			urlBuilder.addQueryParameters(queryParamsSerializer.serializePagination(queryParams));
			urlBuilder.addQueryParameters(queryParamsSerializer.serializeIncludedFields(queryParams));
			urlBuilder.addQueryParameters(queryParamsSerializer.serializeIncludedRelations(queryParams));
		}
		return urlBuilder.toString();
	}

	class UrlParameterBuilder {

		private StringBuilder builder = new StringBuilder();

		private boolean firstParam;

		private String encoding = "UTF-8";

		public UrlParameterBuilder(String baseUrl) {
			builder.append(baseUrl);
			firstParam = !baseUrl.contains("?");
		}

		@Override
		public String toString() {
			return builder.toString();
		}

		private void addQueryParameters(Map<String, ?> params) {
			if (params != null && !params.isEmpty()) {
				for (Map.Entry<String, ?> entry : params.entrySet()) {
					String key = entry.getKey();
					Object value = entry.getValue();
					addQueryParameter(key, value);
				}
			}
		}

		public void addQueryParameter(String key, String value) {
			if (firstParam) {
				builder.append("?");
				firstParam = false;
			}
			else {
				builder.append("&");
			}
			builder.append(key);
			builder.append("=");
			try {
				builder.append(URLEncoder.encode(value, encoding));
			}
			catch (UnsupportedEncodingException e) {
				throw new IllegalStateException(e);
			}
		}

		private void addQueryParameter(String key, Object value) {
			if (value instanceof Collection) {
				for (Object element : (Collection<?>) value) {
					addQueryParameter(key, (String) element);
				}
			}
			else {
				addQueryParameter(key, (String) value);
			}
		}
	}
}
