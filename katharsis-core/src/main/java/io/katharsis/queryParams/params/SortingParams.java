package io.katharsis.queryParams.params;

import io.katharsis.queryParams.RestrictedSortingValues;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;

@Value
public class SortingParams {
    private Map<String, RestrictedSortingValues> params = new HashMap<>();

    public SortingParams(Map<String, RestrictedSortingValues> params) {
        this.params.putAll(params);
    }

}
