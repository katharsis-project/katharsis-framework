package io.katharsis.queryspec.internal;

import java.util.Map;
import java.util.Set;

import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.queryParams.context.SimpleQueryParamsParserContext;
import io.katharsis.resource.information.ResourceInformation;
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
		ResourceInformation info = resourceRegistry.getEntry(resourceClass).getResourceInformation();
		SimpleQueryParamsParserContext context = new SimpleQueryParamsParserContext(parameters, info);
		return new QueryParamsAdapter(resourceClass, queryParamsBuilder.buildQueryParams(context), resourceRegistry);
	}
}
