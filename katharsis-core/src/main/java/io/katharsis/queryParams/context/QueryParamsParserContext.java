package io.katharsis.queryParams.context;

import io.katharsis.resource.information.ResourceInformation;

import java.util.Set;

/**
 * Supplies information about the query parameters of the incoming request.  This information is then
 * used by QueryParamsParsers to create QueryParams objects.
 */
public interface QueryParamsParserContext {

    /**
     * Returns the set of parameter values that match the given query parameter name of the current request.
     */
    Set<String> getParameterValue(String parameterName);

    /**
     * Returns the set of query parameter names associated to the current request.
     */
    Iterable<String> getParameterNames();

    /**
     * Returns ResourceInformation for the primary resource of the current request.
     */
    ResourceInformation getRequestedResourceInformation();
}
