package io.katharsis.core.internal.query;

import java.util.Map;
import java.util.Set;

import io.katharsis.queryspec.QuerySpecDeserializer;
import io.katharsis.queryspec.QuerySpecDeserializerContext;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.resource.registry.ResourceRegistry;

public class QuerySpecAdapterBuilder implements QueryAdapterBuilder {

	private QuerySpecDeserializer querySpecDeserializer;

	private ResourceRegistry resourceRegistry;

	public QuerySpecAdapterBuilder(QuerySpecDeserializer querySpecDeserializer, final ResourceRegistry resourceRegistry) {
		this.querySpecDeserializer = querySpecDeserializer;
		this.resourceRegistry = resourceRegistry;
		this.querySpecDeserializer.init(new QuerySpecDeserializerContext(){

			@Override
			public ResourceRegistry getResourceRegistry() {
				return resourceRegistry;
			}});
	}

	@Override
	public QueryAdapter build(Class<?> resourceClass, Map<String, Set<String>> parameters) {
		return new QuerySpecAdapter(querySpecDeserializer.deserialize(resourceClass, parameters), resourceRegistry);
	}
}
