package io.katharsis.dispatcher.filter;

import io.katharsis.response.BaseResponseContext;

/**
 * Manages the chain of filters and their application to a request.
 */
public interface FilterChain {

	/**
	 * Executes the next filter in the request chain or the actual {@link BaseController} once all filters
	 * have been invoked.
	 */
	public BaseResponseContext doFilter(FilterRequestContext filterRequestContext);
}
