package io.katharsis.queryParams.params;

import lombok.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Value
public class FilterParams {

    private final Map<String, Set<String>> params = new HashMap<>();

    public FilterParams(Map<String, Set<String>> params) {
        this.params.putAll(params);
    }

}
