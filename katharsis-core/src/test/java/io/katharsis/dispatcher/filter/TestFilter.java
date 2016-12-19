package io.katharsis.dispatcher.filter;

import io.katharsis.dispatcher.controller.Response;

public class TestFilter implements Filter {

	@Override
	public Response filter(FilterRequestContext filterRequestContext, FilterChain chain) {
		return chain.doFilter(filterRequestContext);
	}
}
