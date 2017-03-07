package io.katharsis.legacy.queryParams.params;

import java.util.Set;

import io.katharsis.core.internal.utils.CompareUtils;

public class IncludedFieldsParams {
    private Set<String> params;

    public IncludedFieldsParams(Set<String> params) {
        this.params = params;
    }

    public Set<String> getParams() {
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IncludedFieldsParams that = (IncludedFieldsParams) o;

        return CompareUtils.isEquals(params, that.params);
    }

    @Override
    public int hashCode() {
        return params != null ? params.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "IncludedFieldsParams{" +
            "params=" + params +
            '}';
    }
}
