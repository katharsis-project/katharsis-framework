package io.katharsis.queryParams.params;

import java.util.Set;

public class IncludedFieldsParams {
    private Set<String> params;

    public IncludedFieldsParams(Set<String> params) {
        this.params = params;
    }

    public Set<String> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "IncludedFieldsParams{" +
            "params=" + params +
            '}';
    }
}
