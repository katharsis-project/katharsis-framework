package io.katharsis.repository.filter;

import io.katharsis.repository.response.Response;

/**
 * Allows to intercept and modify incoming requests and responses. This is 
 * a low-level interface getting called early with the actual
 * request data structures. 
 */
public interface DocumentFilter {

	/**
	 * Filters an incoming request. To continue processing the request, {@link DocumentFilterChain#doFilter(DocumentFilterContext)} must
	 * be called. Information about the request is available from {@link DocumentFilterContext}.
	 *  
	 * @param filterRequestContext request context
	 * @param chain next filters
	 * @return response
	 */
	Response filter(DocumentFilterContext filterRequestContext, DocumentFilterChain chain);

}
