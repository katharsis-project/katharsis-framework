package io.katharsis.repository.filter;

import io.katharsis.repository.response.Response;

/**
 * Manages the chain of filters and their application to a request.
 */
public interface DocumentFilterChain {

	/**
	 * Executes the next filter in the request chain or the actual {@link io.katharsis.core.internal.dispatcher.controller.BaseController} once all filters
	 * have been invoked.
	 *
	 * @param filterRequestContext request context
	 * @return new execution context
	 */
	Response doFilter(DocumentFilterContext filterRequestContext);
}
