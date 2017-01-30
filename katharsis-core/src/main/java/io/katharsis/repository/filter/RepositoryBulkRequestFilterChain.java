package io.katharsis.repository.filter;

import java.util.Map;

import io.katharsis.repository.response.JsonApiResponse;

/**
 * Manages the chain of repository filters to perform a bulk request.
 * 
 * @param <K> key type used to distinguish bulk request items.
 */
public interface RepositoryBulkRequestFilterChain<K> {

	/**
	 * Invokes the next filter in the chain or the actual repository once all filters
	 * have been invoked.
	 *
	 * @param context holding the request and other information.
	 */
	public Map<K, JsonApiResponse> doFilter(RepositoryFilterContext context);

}
