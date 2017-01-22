package io.katharsis.queryspec;

import io.katharsis.legacy.queryParams.QueryParams;

/**
 * Converts QueryParams to QuerySpec to ease parameter handling.
 * 
 * @deprecated no longer needed in the future
 */
@Deprecated
public interface QuerySpecConverter {

	/**
	 * 
	 * @param rootType
	 *            type of the root resources being requested
	 * @param params
	 *            the QueryParams to be converted
	 * @return QuerySpec
	 */
	public QuerySpec fromParams(Class<?> rootType, QueryParams params);

}
