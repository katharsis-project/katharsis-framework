package io.katharsis.queryParams;

import io.katharsis.resource.RestrictedQueryParamsMembers;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryParamsBuilderTest {

    @Test
    public void onGivenFiltersBuilderShouldReturnRequestParamsWithFilters() {
        // GIVEN
        Map<String, List<Object>> queryParams = new HashMap<>();
        queryParams.put(RestrictedQueryParamsMembers.filter.toString(), Collections.singletonList("sampleKey"));

        QueryParamsBuilder sut = new QueryParamsBuilder();

        // WHEN
        RequestParams result = sut.buildRequestParams(queryParams);

        // THEN
        assertThat(result.getFilters()).isNotNull();
    }
}
