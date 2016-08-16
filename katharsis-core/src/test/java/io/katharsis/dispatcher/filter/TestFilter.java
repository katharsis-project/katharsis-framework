package io.katharsis.dispatcher.filter;

import io.katharsis.response.BaseResponseContext;

public class TestFilter implements Filter {

	@Override
	public BaseResponseContext filter(FilterRequestContext filterRequestContext, FilterChain chain) {
		return chain.doFilter(filterRequestContext);
	}
}
