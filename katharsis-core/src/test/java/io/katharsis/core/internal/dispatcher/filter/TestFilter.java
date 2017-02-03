package io.katharsis.core.internal.dispatcher.filter;

import io.katharsis.repository.filter.DocumentFilter;
import io.katharsis.repository.filter.DocumentFilterChain;
import io.katharsis.repository.filter.DocumentFilterContext;
import io.katharsis.repository.response.Response;

public class TestFilter implements DocumentFilter {

	@Override
	public Response filter(DocumentFilterContext filterRequestContext, DocumentFilterChain chain) {
		return chain.doFilter(filterRequestContext);
	}
}
