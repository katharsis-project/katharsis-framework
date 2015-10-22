package io.katharsis.queryParams.params;

import java.util.Set;

public class GroupingParams {
    private Set<String> params;

    public GroupingParams(Set<String> params) {
        this.params = params;
    }

    public Set<String> getParams() {
        return params;
    }
}
