package io.katharsis.repository.filter;

import io.katharsis.core.internal.dispatcher.path.JsonPath;
import io.katharsis.legacy.internal.RepositoryMethodParameterProvider;
import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.repository.request.QueryAdapter;
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
