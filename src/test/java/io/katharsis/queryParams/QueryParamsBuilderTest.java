package io.katharsis.queryParams;

import io.katharsis.jackson.exception.ParametersDeserializationException;
import io.katharsis.queryParams.include.Inclusion;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryParamsBuilderTest {

    private Map<String, Set<String>> queryParams;
    private QueryParamsBuilder sut;

    @Before
    public void prepare() {
        queryParams = new HashMap<>();
        sut = new QueryParamsBuilder();
    }

    @Test
    public void onGivenFiltersBuilderShouldReturnRequestParamsWithFilters() throws ParametersDeserializationException {
        // GIVEN
        queryParams.put("filter[name]", Collections.singleton("John"));

        // WHEN
        QueryParams result = sut.buildQueryParams(queryParams);

        // THEN
        assertThat(result.getFilters()
            .get("name")).isEqualTo(Collections.singleton("John"));
    }

    @Test
    public void onGivenSortingBuilderShouldReturnRequestParamsWithSorting() throws ParametersDeserializationException {
        // GIVEN
        queryParams.put("sort[name]", Collections.singleton("asc"));

        // WHEN
        QueryParams result = sut.buildQueryParams(queryParams);

        // THEN
        assertThat(result.getSorting()
            .get("name")).isEqualTo(RestrictedSortingValues.asc);
    }

    @Test
    public void onGivenGroupingBuilderShouldReturnRequestParamsWithGrouping() throws
        ParametersDeserializationException {
        // GIVEN
        queryParams.put("group", Collections.singleton("name"));

        // WHEN
        QueryParams result = sut.buildQueryParams(queryParams);

        // THEN
        assertThat(result.getGrouping()
            .contains("name")).isTrue();
    }


    @Test
    public void onGivenPaginationBuilderShouldReturnRequestParamsWithPagination() throws
        ParametersDeserializationException {
        // GIVEN
        queryParams.put("page[offset]", Collections.singleton("0"));
        queryParams.put("page[limit]", Collections.singleton("10"));

        // WHEN
        QueryParams result = sut.buildQueryParams(queryParams);

        // THEN
        assertThat(result.getPagination()
            .get(RestrictedPaginationKeys.offset)).isEqualTo(0);
        assertThat(result.getPagination()
            .get(RestrictedPaginationKeys.limit)).isEqualTo(10);
    }

    @Test
    public void onGivenIncludedFieldsBuilderShouldReturnRequestParamsWithIncludedFields() throws
        ParametersDeserializationException {
        // GIVEN
        queryParams.put("fields[name]", Collections.singleton(""));

        // WHEN
        QueryParams result = sut.buildQueryParams(queryParams);

        // THEN
        assertThat(result.getIncludedFields()
            .contains("name")).isTrue();
    }

    @Test
    public void onGivenIncludedRelationsBuilderShouldReturnRequestParamsWithIncludedRelations() throws
        ParametersDeserializationException {
        // GIVEN
        queryParams.put("include[friends]", Collections.singleton(""));

        // WHEN
        QueryParams result = sut.buildQueryParams(queryParams);

        // THEN
        assertThat(result.getIncludedRelations()
            .contains(new Inclusion("friends"))).isTrue();
    }
}
