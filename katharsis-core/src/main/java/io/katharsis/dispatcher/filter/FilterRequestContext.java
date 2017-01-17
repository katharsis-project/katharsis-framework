package io.katharsis.dispatcher.filter;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.path.JsonPath;
import io.katharsis.resource.Document;

/**
 * Provides request information to {@link Filter}.
 */
public interface FilterRequestContext {

	Document getRequestBody();

	RepositoryMethodParameterProvider getParameterProvider();

	QueryParams getQueryParams();

	JsonPath getJsonPath();

	QueryAdapter getQueryAdapter();

	String getMethod();

}
