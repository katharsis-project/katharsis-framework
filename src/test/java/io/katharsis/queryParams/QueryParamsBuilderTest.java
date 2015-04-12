package io.katharsis.queryParams;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.jackson.exception.JsonDeserializationException;
import io.katharsis.resource.RestrictedQueryParamsMembers;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryParamsBuilderTest {

    private Map<String, String> queryParams;
    QueryParamsBuilder sut;

    @Before
    public void prepare() {
        queryParams = new HashMap<>();
        sut = new QueryParamsBuilder(new ObjectMapper());
    }

    @Test
    public void onGivenFiltersBuilderShouldReturnRequestParamsWithFilters() throws JsonDeserializationException {
        // GIVEN
        queryParams.put(RestrictedQueryParamsMembers.filter.name(), "{\"name\": \"John\"}");

        // WHEN
        RequestParams result = sut.buildRequestParams(queryParams);

        // THEN
        assertThat(result.getFilters().get("name").asText()).isEqualTo("John");
    }

    @Test
    public void onGivenSortingBuilderShouldReturnRequestParamsWithSorting() throws JsonDeserializationException {
        // GIVEN
        queryParams.put(RestrictedQueryParamsMembers.sort.name(), "{\"name\": \"asc\"}");

        // WHEN
        RequestParams result = sut.buildRequestParams(queryParams);

        // THEN
        assertThat(result.getSorting().get("name").asText()).isEqualTo("asc");
    }

    @Test
    public void onGivenGroupingBuilderShouldReturnRequestParamsWithGrouping() throws JsonDeserializationException {
        // GIVEN
        queryParams.put(RestrictedQueryParamsMembers.group.name(), "[\"name\"]");

        // WHEN
        RequestParams result = sut.buildRequestParams(queryParams);

        // THEN
        assertThat(result.getGrouping().contains("name")).isTrue();
    }


    @Test
    public void onGivenPaginationBuilderShouldReturnRequestParamsWithPagination() throws JsonDeserializationException {
        // GIVEN
        queryParams.put(RestrictedQueryParamsMembers.page.name(), "{\"page\": \"1\"}");

        // WHEN
        RequestParams result = sut.buildRequestParams(queryParams);

        // THEN
        assertThat(result.getPagination().get("page").asInt()).isEqualTo(1);
    }

    @Test
    public void onGivenIncludedFieldsBuilderShouldReturnRequestParamsWithIncludedFields() throws
            JsonDeserializationException {
        // GIVEN
        queryParams.put(RestrictedQueryParamsMembers.fields.name(), "[\"name\"]");

        // WHEN
        RequestParams result = sut.buildRequestParams(queryParams);

        // THEN
        assertThat(result.getIncludedFields().contains("name")).isTrue();
    }

    @Test
    public void onGivenIncludedRelationsBuilderShouldReturnRequestParamsWithIncludedRelations() throws
            JsonDeserializationException {
        // GIVEN
        queryParams.put(RestrictedQueryParamsMembers.include.name(), "[\"friends\"]");

        // WHEN
        RequestParams result = sut.buildRequestParams(queryParams);

        // THEN
        assertThat(result.getIncludedRelations().contains("friends")).isTrue();
    }
}
