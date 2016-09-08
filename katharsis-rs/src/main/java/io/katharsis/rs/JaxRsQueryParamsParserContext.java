package io.katharsis.rs;

import io.katharsis.queryParams.context.AbstractQueryParamsParserContext;
import io.katharsis.request.path.JsonPath;
import io.katharsis.resource.registry.ResourceRegistry;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.*;


public class JaxRsQueryParamsParserContext extends AbstractQueryParamsParserContext {

    private final Map<String, Set<String>> queryParameters = new HashMap<>();

    JaxRsQueryParamsParserContext(UriInfo uriInfo, ResourceRegistry resourceRegistry, JsonPath path) {
        super(resourceRegistry, path);
        initParameterMap(uriInfo);
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

    private void initParameterMap(UriInfo uriInfo) {
        MultivaluedMap<String, String> queryParametersMultiMap = uriInfo.getQueryParameters();

        for (Map.Entry<String, List<String>> queryEntry : queryParametersMultiMap.entrySet()) {
            queryParameters.put(queryEntry.getKey(), new LinkedHashSet<>(queryEntry.getValue()));
        }
    }
}
