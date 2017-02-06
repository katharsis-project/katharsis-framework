package io.katharsis.spring.legacy;

import io.katharsis.core.internal.dispatcher.path.JsonPath;
import io.katharsis.legacy.queryParams.context.AbstractQueryParamsParserContext;
import io.katharsis.legacy.queryParams.context.QueryParamsParserContext;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.ResourceRegistry;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class SpringQueryParamsParserContext extends AbstractQueryParamsParserContext {

    private final Map<String, Set<String>> queryParameters = new HashMap<>();

    SpringQueryParamsParserContext(HttpServletRequest request, ResourceRegistry resourceRegistry, JsonPath path) {
        super(resourceRegistry, path);
        initParameterMap(request);
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

    private void initParameterMap(HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            queryParameters.put(entry.getKey(), new HashSet<>(Arrays.asList(entry.getValue())));
        }
    }
}
