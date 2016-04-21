package io.katharsis.queryParams.params;

import java.util.HashSet;
import java.util.Set;

public class GroupingParams {
    private Set<String> params = new HashSet<>();

    public GroupingParams(Set<String> params) {
        this.params = params;
    }

    public Set<String> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "GroupingParams{" +
            "params=" + params +
            '}';
    }
}
