package io.katharsis.queryspec.internal;

import java.util.Map;
import java.util.Set;

/**
 * Builds the query adapter for the given parameters, resulting in either a queryParams or querySpec adapter depending on the chosen implementation.
 */
public interface QueryAdapterBuilder {

	public QueryAdapter build(Class<?> resourceClass, Map<String, Set<String>> parameters);

}
