package io.katharsis.queryParams.params;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic query parameter container
 *
 * @param <T> type of the parameter
 */
public class TypedParams<T> {
    private Map<String, T> params = new HashMap<>();

    public TypedParams(Map<String, T> params) {
        this.params = params;
    }

    public Map<String, T> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "TypedParams{" +
            "params=" + params +
            '}';
    }
}
