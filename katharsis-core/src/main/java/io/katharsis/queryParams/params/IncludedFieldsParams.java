package io.katharsis.queryParams.params;

import lombok.Value;

import java.util.Set;

@Value
public class IncludedFieldsParams {
    private Set<String> params;

    public IncludedFieldsParams(Set<String> params) {
        this.params = params;
    }

    public Set<String> getParams() {
        return params;
    }

}
