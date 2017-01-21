package io.katharsis.legacy.internal;

import java.util.Map;
import java.util.Set;

import io.katharsis.core.internal.query.QueryAdapterBuilder;
import io.katharsis.legacy.queryParams.QueryParamsBuilder;
import io.katharsis.legacy.queryParams.context.SimpleQueryParamsParserContext;
import io.katharsis.queryspec.internal.QueryAdapter;
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
