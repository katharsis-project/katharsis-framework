package io.katharsis.client.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.HttpUrl.Builder;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.StringUtils;

public class RequestUrlBuilder {

	private QueryParamsSerializer queryParamsSerializer = new DefaultQueryParamsSerializer();

	private ResourceRegistry resourceRegistry;

	public RequestUrlBuilder(ResourceRegistry resourceRegistry) {
		this.resourceRegistry = resourceRegistry;
	}

	public <T> HttpUrl buildUrl(Class<T> resourceClass, Object id, QueryParams queryParams) {
		return buildUrl(resourceClass, id, queryParams, null);
	}
	
	public <T> HttpUrl buildUrl(Class<T> resourceClass, Object id, QueryParams queryParams, String relationshipName) {
		String strRepoUrl = resourceRegistry.getResourceUrl(resourceClass);
		if (!strRepoUrl.endsWith("/")) {
			strRepoUrl += "/";
		}
		HttpUrl repositoryUrl = HttpUrl.parse(strRepoUrl);

		HttpUrl.Builder urlBuilder = new HttpUrl.Builder();
		urlBuilder.host(repositoryUrl.host());
		urlBuilder.port(repositoryUrl.port());
		urlBuilder.scheme(repositoryUrl.scheme());
		
		RegistryEntry<?> entry = resourceRegistry.getEntry(resourceClass);
		ResourceInformation resourceInformation = entry.getResourceInformation();
		
		String encodedPath;
		if (id instanceof Collection) {
			Collection<?> ids = (Collection<?>) id;
			Collection<String> strIds = new ArrayList<String>();
			for (Object idElem : ids) {
				String strIdElem = resourceInformation.toIdString(idElem);
				strIds.add(strIdElem);
			}
			encodedPath = repositoryUrl.encodedPath() + StringUtils.join(",", strIds) + "/";
		} else if (id != null) {
			String strId = resourceInformation.toIdString(id);
			encodedPath = repositoryUrl.encodedPath() +  strId + "/";
		} else {
			encodedPath = repositoryUrl.encodedPath();
		}

		if (queryParams != null) {
			addParams(urlBuilder, queryParamsSerializer.serializeFilters(queryParams));
			addParams(urlBuilder, queryParamsSerializer.serializeSorting(queryParams));
			addParams(urlBuilder, queryParamsSerializer.serializeGrouping(queryParams));
			addParams(urlBuilder, queryParamsSerializer.serializePagination(queryParams));
			addParams(urlBuilder, queryParamsSerializer.serializeIncludedFields(queryParams));
			addParams(urlBuilder, queryParamsSerializer.serializeIncludedRelations(queryParams));
		}
		
		if(relationshipName != null){
			encodedPath += "relationships/" + relationshipName + "/";
		}
		
		urlBuilder.encodedPath(encodedPath);

		return urlBuilder.build();
	}

	private void addParams(Builder urlBuilder, Map<String, ?> params) {
		if (params != null && !params.isEmpty()) {
			for (Map.Entry<String, ?> entry : params.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (value instanceof Collection) {
					for (Object element : (Collection<?>) value) {
						urlBuilder.addQueryParameter(key, (String) element);
					}
				} else {
					urlBuilder.addQueryParameter(key, (String) value);
				}
			}
		}
	}

}
