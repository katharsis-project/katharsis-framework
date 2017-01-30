package io.katharsis.repository.filter;

import io.katharsis.resource.meta.MetaInformation;

/**
 * Manages the chain of repository filters to resolve meta information.
 */
public interface RepositoryMetaFilterChain {

	/**
	 * Invokes the next filter in the chain or the actual repository once all filters
	 * have been invoked.
	 *
	 * @param context holding the request and other information.
	 * @param resources for which to compute the meta information (as a whole, not for the individual items)
	 * @return filtered meta information
	 */
	public <T> MetaInformation doFilter(RepositoryFilterContext context, Iterable<T> resources);

}
