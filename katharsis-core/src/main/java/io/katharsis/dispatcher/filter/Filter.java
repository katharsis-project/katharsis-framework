package io.katharsis.dispatcher.filter;

import io.katharsis.dispatcher.controller.Response;

/**
 * Allows to intercept and modify incoming requests and responses. This is 
 * a low-level interface getting called early with the actual
 * request data structures. 
 */
public interface Filter {

	/**
	 * Filters an incoming request. To continue processing the request, {@link FilterChain#doFilter(FilterRequestContext)} must
	 * be called. Information about the request is available from {@link FilterRequestContext}.
	 *  
	 * @param filterRequestContext request context
	 * @param chain next filters
	 * @return response
	 */
	Response filter(FilterRequestContext filterRequestContext, FilterChain chain);

}
