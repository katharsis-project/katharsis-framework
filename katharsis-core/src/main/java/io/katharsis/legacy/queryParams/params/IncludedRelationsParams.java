package io.katharsis.legacy.queryParams.params;

import java.util.Set;

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
    public String toString() {
        return "IncludedRelationsParams{" +
            "params=" + params +
            '}';
    }
}
