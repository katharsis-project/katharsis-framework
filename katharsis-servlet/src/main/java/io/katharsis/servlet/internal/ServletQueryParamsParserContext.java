package io.katharsis.servlet.internal;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import io.katharsis.core.internal.dispatcher.path.JsonPath;
import io.katharsis.invoker.internal.KatharsisInvokerContext;
import io.katharsis.legacy.queryParams.context.AbstractQueryParamsParserContext;
import io.katharsis.resource.registry.ResourceRegistry;

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
