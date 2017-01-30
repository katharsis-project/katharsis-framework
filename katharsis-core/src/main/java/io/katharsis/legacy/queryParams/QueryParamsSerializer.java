package io.katharsis.legacy.queryParams;

import java.util.Map;
import java.util.Set;

/**
 * Converts {@link QueryParams} into URL parameters.
 *
 *
 * @deprecated make use of QuerySpec
 */
@Deprecated
public interface QueryParamsSerializer {

	public Map<String, Set<String>> serializeFilters(QueryParams queryParams);

	public Map<String, Set<String>> serializeGrouping(QueryParams queryParams);

	public Map<String, String> serializeSorting(QueryParams queryParams);

	public Map<String, Set<String>> serializeIncludedFields(QueryParams queryParams);

	public Map<String, Set<String>> serializeIncludedRelations(QueryParams queryParams);

	public Map<String, String> serializePagination(QueryParams queryParams);

}
