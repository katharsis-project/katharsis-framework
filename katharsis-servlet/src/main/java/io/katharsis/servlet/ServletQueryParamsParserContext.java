package io.katharsis.servlet;

import io.katharsis.invoker.KatharsisInvokerContext;
import io.katharsis.queryParams.context.AbstractQueryParamsParserContext;
import io.katharsis.queryParams.context.QueryParamsParserContext;
import io.katharsis.request.path.JsonPath;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.servlet.util.QueryStringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ServletQueryParamsParserContext extends AbstractQueryParamsParserContext {

    private final Map<String, Set<String>> queryParameters;

    public ServletQueryParamsParserContext(KatharsisInvokerContext invokerContext, ResourceRegistry resourceRegistry,
                                           JsonPath path) {
        super(resourceRegistry, path);
        queryParameters = QueryStringUtils.parseQueryStringAsSingleValueMap(invokerContext);
    }

    @Override
    public Set<String> getParameterValue(String parameterName) {
        if (queryParameters.containsKey(parameterName)) {
            return queryParameters.get(parameterName);
        }
        return Collections.emptySet();
    }

    @Override
    public Iterable<String> getParameterNames() {
        return queryParameters.keySet();
    }
}
