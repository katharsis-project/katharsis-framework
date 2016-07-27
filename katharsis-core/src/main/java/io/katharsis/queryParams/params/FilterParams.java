package io.katharsis.queryParams.params;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FilterParams {
    private Map<String, Set<String>> params = new HashMap<>();

    public FilterParams(Map<String, Set<String>> params) {
        this.params = params;
    }

    public Map<String, Set<String>> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "FilterParams{" +
            "params=" + params +
            '}';
    }
}
