package io.katharsis.queryParams.params;

import io.katharsis.queryParams.RestrictedSortingValues;

import java.util.HashMap;
import java.util.Map;

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
