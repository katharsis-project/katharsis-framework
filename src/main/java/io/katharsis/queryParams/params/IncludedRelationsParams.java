package io.katharsis.queryParams.params;

import io.katharsis.queryParams.include.Inclusion;

import java.util.Set;

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
