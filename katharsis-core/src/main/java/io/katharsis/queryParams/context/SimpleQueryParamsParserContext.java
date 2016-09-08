package io.katharsis.queryParams.context;


import io.katharsis.resource.information.ResourceInformation;

import java.util.Map;
import java.util.Set;

/**
 * A QueryParamsParserContext implementation mainly used for testing purposes.
 * This implementation is a simple wrapper over a map of query parameters and their values.
 */
public class SimpleQueryParamsParserContext implements QueryParamsParserContext {

    private final Map<String, Set<String>> paramMap;
    private final ResourceInformation resourceInformation;

    public SimpleQueryParamsParserContext(Map<String, Set<String>> paramMap) {
        this(paramMap, null);
    }

    public SimpleQueryParamsParserContext(Map<String, Set<String>> paramMap, ResourceInformation resourceInformation) {
        this.paramMap = paramMap;
        this.resourceInformation = resourceInformation;
    }

    @Override
    public Set<String> getParameterValue(String parameterName) {
        return paramMap.get(parameterName);
    }

    @Override
    public Iterable<String> getParameterNames() {
        return paramMap.keySet();
    }

    @Override
    public ResourceInformation getRequestedResourceInformation() {
        return resourceInformation;
    }
}
