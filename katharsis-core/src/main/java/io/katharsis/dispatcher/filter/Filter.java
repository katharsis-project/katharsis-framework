package io.katharsis.dispatcher.filter;

import io.katharsis.response.BaseResponseContext;

/**
 * Allows to intercept and modify incoming requests and responses.
 */
public interface Filter {

	/**
	 * Filters an incoming request. To continue processing the request, {@link FilterChain#doFilter(FilterRequestContext)} must
	 * be called. Information about the request is available from {@link FilterRequestContext}.
	 *  
	 * @param filterRequestContext
	 * @param chain
	 */
	public BaseResponseContext filter(FilterRequestContext filterRequestContext, FilterChain chain);

}
