package io.katharsis.dispatcher.filter;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;

/**
 * Provides request information to {@link Filter}.
 */
public interface FilterRequestContext {

	RequestBody getRequestBody();

	RepositoryMethodParameterProvider getParameterProvider();

	QueryParams getQueryParams();

	JsonPath getJsonPath();

}
