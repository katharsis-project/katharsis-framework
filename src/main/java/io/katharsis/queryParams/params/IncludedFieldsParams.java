package io.katharsis.queryParams.params;

import java.util.Map;
import java.util.Set;

public class IncludedFieldsParams {
    private Map<String, Set<String>> params;

    public IncludedFieldsParams(Map<String, Set<String>> params) {
        this.params = params;
    }

    public Map<String, Set<String>> getParams() {
        return params;
    }
}
