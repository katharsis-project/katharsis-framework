package io.katharsis.core.internal.query;

import java.util.Map;
import java.util.Set;

import io.katharsis.module.ModuleRegistry;
import io.katharsis.queryspec.QuerySpecDeserializer;
import io.katharsis.queryspec.QuerySpecDeserializerContext;
import io.katharsis.repository.request.QueryAdapter;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.parser.TypeParser;

public class QuerySpecAdapterBuilder implements QueryAdapterBuilder {

	private QuerySpecDeserializer querySpecDeserializer;

	private ResourceRegistry resourceRegistry;

	public QuerySpecAdapterBuilder(QuerySpecDeserializer querySpecDeserializer, final ModuleRegistry moduleRegistry) {
		this.querySpecDeserializer = querySpecDeserializer;
		this.resourceRegistry = moduleRegistry.getResourceRegistry();
		this.querySpecDeserializer.init(new QuerySpecDeserializerContext() {

			@Override
			public ResourceRegistry getResourceRegistry() {
				return resourceRegistry;
			}

			@Override
			public TypeParser getTypeParser() {
				return moduleRegistry.getTypeParser();
			}
		});
	}

	@Override
	public QueryAdapter build(ResourceInformation resourceInformation, Map<String, Set<String>> parameters) {

		return new QuerySpecAdapter(querySpecDeserializer.deserialize(resourceInformation, parameters), resourceRegistry);
	}
}
