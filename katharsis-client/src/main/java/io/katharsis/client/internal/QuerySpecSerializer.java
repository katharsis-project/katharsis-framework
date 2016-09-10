package io.katharsis.client.internal;

import java.util.Map;
import java.util.Set;

import io.katharsis.queryspec.QuerySpec;

/**
 * Converts {@link QuerySpec} into URL parameters.
 */
public interface QuerySpecSerializer {

	public Map<String, Set<String>> serialize(QuerySpec querySpec);

}
