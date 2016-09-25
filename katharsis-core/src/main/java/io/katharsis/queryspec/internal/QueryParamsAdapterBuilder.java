package io.katharsis.queryspec.internal;

import java.util.Map;
import java.util.Set;

import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.resource.registry.ResourceRegistry;

public class QueryParamsAdapterBuilder implements QueryAdapterBuilder {

	private QueryParamsBuilder queryParamsBuilder;
	private ResourceRegistry resourceRegistry;


	public QueryParamsAdapterBuilder(QueryParamsBuilder queryParamsBuilder, ResourceRegistry resourceRegistry) {
		this.queryParamsBuilder = queryParamsBuilder;
		this.resourceRegistry = resourceRegistry;
	}

	@Override
	public QueryAdapter build(Class<?> resourceClass, Map<String, Set<String>> parameters) {
		return new QueryParamsAdapter(resourceClass, queryParamsBuilder.buildQueryParams(parameters), resourceRegistry);
	}
}
