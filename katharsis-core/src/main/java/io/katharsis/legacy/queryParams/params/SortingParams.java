package io.katharsis.legacy.queryParams.params;

import java.util.HashMap;
import java.util.Map;

import io.katharsis.core.internal.utils.CompareUtils;
import io.katharsis.legacy.queryParams.RestrictedSortingValues;

public class SortingParams {
    private Map<String, RestrictedSortingValues> params = new HashMap<>();

    public SortingParams(Map<String, RestrictedSortingValues> params) {
        this.params = params;
    }

    public Map<String, RestrictedSortingValues> getParams() {
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
        SortingParams other = (SortingParams) obj;
        return CompareUtils.isEquals(params, other.params);
    }

    @Override
    public String toString() {
        return "SortingParams{" +
            "params=" + params +
            '}';
    }
}
