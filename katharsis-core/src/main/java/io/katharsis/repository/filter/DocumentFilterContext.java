package io.katharsis.repository.filter;

import io.katharsis.core.internal.dispatcher.path.JsonPath;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.resource.Document;

/**
 * Provides request information to {@link DocumentFilter}.
 */
public interface DocumentFilterContext {

	Document getRequestBody();

	RepositoryMethodParameterProvider getParameterProvider();

	QueryParams getQueryParams();

	JsonPath getJsonPath();

	QueryAdapter getQueryAdapter();

	String getMethod();

}
