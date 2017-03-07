package io.katharsis.legacy.queryParams.params;

import java.util.HashSet;
import java.util.Set;

import io.katharsis.core.internal.utils.CompareUtils;

public class GroupingParams {
    private Set<String> params = new HashSet<>();

    public GroupingParams(Set<String> params) {
        this.params = params;
    }

    public Set<String> getParams() {
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupingParams that = (GroupingParams) o;
        return CompareUtils.isEquals(params, that.params);
    }

    @Override
    public int hashCode() {
        return params != null ? params.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "GroupingParams{" +
            "params=" + params +
            '}';
    }
}
