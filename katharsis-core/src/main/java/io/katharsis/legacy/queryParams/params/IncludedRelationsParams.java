package io.katharsis.legacy.queryParams.params;

import java.util.Set;

import io.katharsis.core.internal.utils.CompareUtils;
import io.katharsis.legacy.queryParams.include.Inclusion;

public class IncludedRelationsParams {
    private Set<Inclusion> params;

    public IncludedRelationsParams(Set<Inclusion> params) {
        this.params = params;
    }

    public Set<Inclusion> getParams() {
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IncludedRelationsParams that = (IncludedRelationsParams) o;

        return CompareUtils.isEquals(params, that.params);
    }

    @Override
    public int hashCode() {
        return params != null ? params.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "IncludedRelationsParams{" +
            "params=" + params +
            '}';
    }
}
