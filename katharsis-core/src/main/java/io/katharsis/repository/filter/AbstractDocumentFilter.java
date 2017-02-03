package io.katharsis.repository.filter;

import io.katharsis.repository.response.Response;

/**
 * Empty {@link DocumentFilter} implementation useful as a starting point to write new filters.
 */
public class AbstractDocumentFilter implements DocumentFilter {

	@Override
	public Response filter(DocumentFilterContext context, DocumentFilterChain chain) {
		return chain.doFilter(context);
	}
}
