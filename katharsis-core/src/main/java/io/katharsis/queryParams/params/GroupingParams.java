package io.katharsis.queryParams.params;

import lombok.Value;

import java.util.HashSet;
import java.util.Set;

@Value
public class GroupingParams {

    private Set<String> params = new HashSet<>();

    public GroupingParams(Set<String> params) {
        this.params.addAll(params);
    }

}
