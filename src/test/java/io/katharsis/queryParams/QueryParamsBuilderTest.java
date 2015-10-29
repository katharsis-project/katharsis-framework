package io.katharsis.queryParams;

import io.katharsis.jackson.exception.ParametersDeserializationException;
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
        queryParams.put("filter[User][name]", Collections.singleton("John"));

        // WHEN
        QueryParams result = sut.buildQueryParams(queryParams);

        // THEN
        assertThat(result.getFilters()
            .getParams()
            .get("User")).isNotNull();

        assertThat(result.getFilters()
            .getParams()
            .get("User")
            .getParams()
            .get("name")).isEqualTo(Collections.singleton("John"));
    }

    @Test
    public void onGivenSortingBuilderShouldReturnRequestParamsWithSorting() throws ParametersDeserializationException {
        // GIVEN
        queryParams.put("sort[User][name]", Collections.singleton("asc"));

        // WHEN
        QueryParams result = sut.buildQueryParams(queryParams);

        // THEN
        assertThat(result.getSorting()
            .getParams()
            .get("User")).isNotNull();

        assertThat(result.getSorting()
            .getParams()
            .get("User")
            .getParams()
            .get("name")).isEqualTo(RestrictedSortingValues.asc);

    }

    @Test
    public void onGivenGroupingBuilderShouldReturnRequestParamsWithGrouping() throws
        ParametersDeserializationException {
        // GIVEN
        queryParams.put("group[User]", Collections.singleton("name"));

        // WHEN
        QueryParams result = sut.buildQueryParams(queryParams);

        // THEN
        assertThat(result.getGrouping()
            .getParams()
            .get("User")).isNotNull();

        assertThat(result.getGrouping()
            .getParams()
            .get("User")
            .getParams()
            .iterator()
            .next()).isEqualTo("name");
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
        queryParams.put("fields[User]", Collections.singleton("name"));

        // WHEN
        QueryParams result = sut.buildQueryParams(queryParams);

        // THEN
        assertThat(result.getIncludedFields()
            .getParams()
            .get("User")).isNotNull();

        assertThat(result.getIncludedFields()
            .getParams()
            .get("User")
            .getParams()
            .iterator()
            .next()).isEqualTo("name");
    }

    @Test
    public void onGivenIncludedRelationsBuilderShouldReturnRequestParamsWithIncludedRelations() throws
        ParametersDeserializationException {
        // GIVEN
        queryParams.put("include[User]", Collections.singleton("friends"));

        // WHEN
        QueryParams result = sut.buildQueryParams(queryParams);

        // THEN
        assertThat(result.getIncludedRelations()
            .getParams()
            .get("User")).isNotNull();

        assertThat(result.getIncludedRelations()
            .getParams()
            .get("User")
            .getParams()
            .iterator()
            .next()
            .getPath()).isEqualTo("friends");
    }
}
