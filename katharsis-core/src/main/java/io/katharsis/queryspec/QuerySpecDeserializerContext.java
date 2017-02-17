package io.katharsis.queryspec;

import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.parser.TypeParser;

public interface QuerySpecDeserializerContext {

	public ResourceRegistry getResourceRegistry();

	public TypeParser getTypeParser();
}
