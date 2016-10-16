package io.katharsis.queryspec;

import java.util.Map;
import java.util.Set;

/**
 * Converts {@link QuerySpec} into URL parameters.
 */
public interface QuerySpecSerializer {

	public Map<String, Set<String>> serialize(QuerySpec querySpec);

}
