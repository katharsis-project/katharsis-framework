package io.katharsis.vertx;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.params.FilterParams;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class QueryParamsUtils {

    public static String getSingleFilterValue(QueryParams queryParams, String resourceType, String paramKey) {
        FilterParams filterParams = queryParams.getFilters().getParams().get(resourceType);
        if (filterParams != null && !isEmpty(filterParams.getParams())) {
            Set<String> filterValues = filterParams.getParams().get(paramKey);
            if (!isEmpty(filterValues)) {
                return filterValues.iterator().next();
            }
        }
        return null;
    }

    public static String getSingleStartWithFilterValue(QueryParams queryParams, String resourceType, String paramKey) {
        return getSingleFilterValue(queryParams, resourceType, paramKey + ".startWith");
    }

    private static boolean isEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }

    private static boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

}
