package io.katharsis.queryspec;

import java.util.Map;
import java.util.Set;

public interface QuerySpecDeserializer {

	QuerySpec deserialize(Class<?> rootType, Map<String, Set<String>> queryParams);
}
