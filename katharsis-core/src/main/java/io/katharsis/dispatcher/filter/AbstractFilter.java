package io.katharsis.dispatcher.filter;

import io.katharsis.dispatcher.controller.Response;

/**
 * Empty {@link Filter} implementation useful as a starting point to write new filters.
 */
public class AbstractFilter implements Filter {

	@Override
	public Response filter(FilterRequestContext context, FilterChain chain) {
		return chain.doFilter(context);
	}
}
