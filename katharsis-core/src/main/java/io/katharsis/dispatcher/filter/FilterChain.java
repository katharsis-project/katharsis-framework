package io.katharsis.dispatcher.filter;

import io.katharsis.dispatcher.controller.Response;

/**
 * Manages the chain of filters and their application to a request.
 */
public interface FilterChain {

	/**
	 * Executes the next filter in the request chain or the actual {@link io.katharsis.dispatcher.controller.BaseController} once all filters
	 * have been invoked.
	 *
	 * @param filterRequestContext request context
	 * @return new execution context
	 */
	Response doFilter(FilterRequestContext filterRequestContext);
}
