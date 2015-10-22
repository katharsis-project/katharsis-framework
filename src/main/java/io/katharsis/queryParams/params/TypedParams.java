package io.katharsis.queryParams.params;

import java.util.Map;

/**
 * Generic query parameter container
 *
 * @param <T>
 */
public class TypedParams<T> {
    private Map<String, T> params;

    public TypedParams(Map<String, T> params) {
        this.params = params;
    }

    public Map<String, T> getParams() {
        return params;
    }
}
