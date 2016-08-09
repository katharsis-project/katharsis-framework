package io.katharsis.dispatcher.filter;

import io.katharsis.response.BaseResponseContext;

/**
 * Empty {@link Filter} implementation useful as a starting point to write new filters.
 */
public class AbstractFilter implements Filter {

	@Override
	public BaseResponseContext filter(FilterRequestContext context, FilterChain chain) {
		return chain.doFilter(context);
	}
}
