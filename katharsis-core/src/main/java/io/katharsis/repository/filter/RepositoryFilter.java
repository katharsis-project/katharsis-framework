package io.katharsis.repository.filter;

import java.util.Map;

import io.katharsis.repository.response.JsonApiResponse;
import io.katharsis.resource.links.LinksInformation;
import io.katharsis.resource.meta.MetaInformation;

/**
 * Allows to intercept calls to repositories by modules and make changes.
 */
public interface RepositoryFilter {

	/**
	 * Filters a regular request.
	 * 
	 * @param context to access request and katharsis information
	 * @param chain to proceed to the next filter resp. actual repository.
	 * @return filtered result to be returned to next filter resp. caller
	 */
	JsonApiResponse filterRequest(RepositoryFilterContext context, RepositoryRequestFilterChain chain);
	
	/**
	 * Filters a bulk request (used to fetch included relationships).
	 * 
	 * @param context to access request and katharsis information
	 * @param chain to proceed to the next filter resp. actual repository.
	 * @return filtered results to be returned to next filter resp. caller
	 */
	<K> Map<K, JsonApiResponse> filterBulkRequest(RepositoryFilterContext context, RepositoryBulkRequestFilterChain<K> chain);

	/**
	 * Filter a result, ban be either a single entity or collection.
	 * 
	 * @param context to access request and katharsis information
	 * @param chain to proceed to the next filter resp. actual repository.
	 * @return filtered result to be returned to next filter resp. caller
	 */
	<T> Iterable<T> filterResult(RepositoryFilterContext context, RepositoryResultFilterChain<T> chain);

	/**
	 * Filters the meta information.
	 * 
	 * @param context to access request and katharsis information
	 * @param chain to proceed to the next filter resp. actual repository.
	 * @return filtered metaInformation to be returned to next filter resp. caller
	 */
	<T> MetaInformation filterMeta(RepositoryFilterContext context, Iterable<T> resources, RepositoryMetaFilterChain chain);

	/**
	 * Filters the links information.
	 * 
	 * @param context to access request and katharsis information
	 * @param chain to proceed to the next filter resp. actual repository.
	 * @return filtered linksInformation to be returned to next filter resp. caller
	 */
	<T> LinksInformation filterLinks(RepositoryFilterContext context, Iterable<T> resources, RepositoryLinksFilterChain chain);

}
