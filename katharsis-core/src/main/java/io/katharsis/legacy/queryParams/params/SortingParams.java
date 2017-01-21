package io.katharsis.legacy.queryParams.params;

import java.util.HashMap;
import java.util.Map;

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
    public String toString() {
        return "SortingParams{" +
            "params=" + params +
            '}';
    }
}
