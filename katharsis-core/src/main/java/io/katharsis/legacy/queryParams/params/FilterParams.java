package io.katharsis.legacy.queryParams.params;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.katharsis.core.internal.utils.CompareUtils;

public class FilterParams {
    private Map<String, Set<String>> params = new HashMap<>();

    public FilterParams(Map<String, Set<String>> params) {
        this.params = params;
    }

    public Map<String, Set<String>> getParams() {
        return params;
    }

    @Override
    public int hashCode() {
        return params != null ? params.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        FilterParams other = (FilterParams) obj;
        return CompareUtils.isEquals(params, other.params);
    }

    @Override
    public String toString() {
        return "FilterParams{" +
            "params=" + params +
            '}';
    }
}
