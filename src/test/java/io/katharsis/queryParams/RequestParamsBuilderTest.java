package io.katharsis.queryParams;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.jackson.exception.ParametersDeserializationException;
import io.katharsis.resource.RestrictedQueryParamsMembers;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RequestParamsBuilderTest {

    private Map<String, String> queryParams;
    RequestParamsBuilder sut;

    @Before
    public void prepare() {
        queryParams = new HashMap<>();
        sut = new RequestParamsBuilder(new ObjectMapper());
    }

    @Test
    public void onGivenFiltersBuilderShouldReturnRequestParamsWithFilters() throws ParametersDeserializationException {
        // GIVEN
        queryParams.put(RestrictedQueryParamsMembers.filter.name(), "{\"name\": \"John\"}");

        // WHEN
        RequestParams result = sut.buildRequestParams(queryParams);

        // THEN
        assertThat(result.getFilters().get("name").asText()).isEqualTo("John");
    }

    @Test
    public void onGivenSortingBuilderShouldReturnRequestParamsWithSorting() throws ParametersDeserializationException {
        // GIVEN
        queryParams.put(RestrictedQueryParamsMembers.sort.name(), "{\"name\": \"asc\"}");

        // WHEN
        RequestParams result = sut.buildRequestParams(queryParams);

        // THEN
        assertThat(result.getSorting().get("name")).isEqualTo(SortingValues.asc);
    }

    @Test
    public void onGivenGroupingBuilderShouldReturnRequestParamsWithGrouping() throws ParametersDeserializationException {
        // GIVEN
        queryParams.put(RestrictedQueryParamsMembers.group.name(), "[\"name\"]");

        // WHEN
        RequestParams result = sut.buildRequestParams(queryParams);

        // THEN
        assertThat(result.getGrouping().contains("name")).isTrue();
    }


    @Test
    public void onGivenPaginationBuilderShouldReturnRequestParamsWithPagination() throws ParametersDeserializationException {
        // GIVEN
        queryParams.put(RestrictedQueryParamsMembers.page.name(), "{ \"offset\" : 0, \"limit\": 10}");

        // WHEN
        RequestParams result = sut.buildRequestParams(queryParams);

        // THEN
        assertThat(result.getPagination().get(PaginationKeys.offset)).isEqualTo(0);
        assertThat(result.getPagination().get(PaginationKeys.limit)).isEqualTo(10);
    }

    @Test
    public void onGivenIncludedFieldsBuilderShouldReturnRequestParamsWithIncludedFields() throws
            ParametersDeserializationException {
        // GIVEN
        queryParams.put(RestrictedQueryParamsMembers.fields.name(), "[\"name\"]");

        // WHEN
        RequestParams result = sut.buildRequestParams(queryParams);

        // THEN
        assertThat(result.getIncludedFields().contains("name")).isTrue();
    }

    @Test
    public void onGivenIncludedRelationsBuilderShouldReturnRequestParamsWithIncludedRelations() throws
            ParametersDeserializationException {
        // GIVEN
        queryParams.put(RestrictedQueryParamsMembers.include.name(), "[\"friends\"]");

        // WHEN
        RequestParams result = sut.buildRequestParams(queryParams);

        // THEN
        assertThat(result.getIncludedRelations().contains("friends")).isTrue();
    }
}
