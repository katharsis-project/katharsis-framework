package io.katharsis.queryspec;

import java.util.Map;
import java.util.Set;

import io.katharsis.resource.information.ResourceInformation;

public interface QuerySpecDeserializer {

	void init(QuerySpecDeserializerContext ctx);

	QuerySpec deserialize(ResourceInformation resourceInformation, Map<String, Set<String>> queryParams);
}
