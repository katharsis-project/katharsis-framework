package io.katharsis.queryParams;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultQueryParamsParserTest {

    private Map<String, Set<String>> queryParams;
    private QueryParamsParser parser = new DefaultQueryParamsParser();

    @Before
    public void prepare() {
        queryParams = new HashMap<>();
    }

    @Test
    public void onGivenFiltersParserShouldReturnOnlyRequestParamsWithFilters() {
        // GIVEN
        queryParams.put("filter[users][name]", Collections.singleton("John"));
        queryParams.put("random[users][name]", Collections.singleton("John"));

        // WHEN
        Map<String, Set<String>> result = parser.parseFiltersParameters(queryParams);

        // THEN
        assertThat(result.entrySet().size()).isEqualTo(1);
        assertThat(result.entrySet().iterator().next().getKey().startsWith("filter"));
        assertThat(result.entrySet().iterator().next().getValue().equals(Collections.singleton("John")));
    }

    @Test
    public void onGivenSortingParserShouldReturnOnlyRequestParamsWithSorting() {
        // GIVEN
        queryParams.put("sort[users][name]", Collections.singleton("asc"));
        queryParams.put("random[users][name]", Collections.singleton("desc"));

        // WHEN
        Map<String, Set<String>> result = parser.parseSortingParameters(queryParams);

        // THEN
        assertThat(result.entrySet().size()).isEqualTo(1);
        assertThat(result.entrySet().iterator().next().getKey().startsWith("sort"));
        assertThat(result.entrySet().iterator().next().getValue().equals(Collections.singleton("asc")));
    }

    @Test
    public void onGivenGroupingParserShouldReturnOnlyRequestParamsWithGrouping() {
        // GIVEN
        queryParams.put("group[users]", Collections.singleton("name"));
        queryParams.put("random[users]", Collections.singleton("surname"));

        // WHEN
        Map<String, Set<String>> result = parser.parseGroupingParameters(queryParams);

        // THEN
        assertThat(result.entrySet().size()).isEqualTo(1);
        assertThat(result.entrySet().iterator().next().getKey().startsWith("group"));
        assertThat(result.entrySet().iterator().next().getValue().equals(Collections.singleton("name")));
    }

    @Test
    public void onGivenPaginationParserShouldReturnOnlyRequestParamsWithPagination() {
        // GIVEN
        queryParams.put("page[offset]", Collections.singleton("1"));
        queryParams.put("page[limit]", Collections.singleton("10"));
        queryParams.put("random[offset]", Collections.singleton("2"));
        queryParams.put("random[limit]", Collections.singleton("20"));

        // WHEN
        Map<String, Set<String>> result = parser.parsePaginationParameters(queryParams);

        // THEN
        assertThat(result.entrySet().size()).isEqualTo(2);
        assertThat(result.get("page[offset]").equals(Collections.singleton("1")));
        assertThat(result.get("page[limit]").equals(Collections.singleton("10")));
    }

    ////////
    @Test
    public void onGivenIncludedFieldsParserShouldReturnOnlyRequestParamsWithIncludedFields() {
        // GIVEN
        queryParams.put("fields[users]", Collections.singleton("name"));
        queryParams.put("random[users]", Collections.singleton("surname"));

        // WHEN
        Map<String, Set<String>> result = parser.parseIncludedFieldsParameters(queryParams);

        // THEN
        assertThat(result.entrySet().size()).isEqualTo(1);
        assertThat(result.entrySet().iterator().next().getKey().startsWith("fields"));
        assertThat(result.entrySet().iterator().next().getValue().equals(Collections.singleton("name")));
    }

    @Test
    public void onGivenIncludedRelationsParserShouldReturnOnlyRequestParamsWithIncludedRelations() {
        // GIVEN
        queryParams.put("include[user]", Collections.singleton("name"));
        queryParams.put("random[user]", Collections.singleton("surname"));

        // WHEN
        Map<String, Set<String>> result = parser.parseIncludedRelationsParameters(queryParams);

        // THEN
        assertThat(result.entrySet().size()).isEqualTo(1);
        assertThat(result.entrySet().iterator().next().getKey().startsWith("include"));
        assertThat(result.entrySet().iterator().next().getValue().equals(Collections.singleton("name")));
    }

    @Test
    public void testIncludeRelationshipWithPathShouldIncludePathInTheResult() throws Exception {
        // GIVEN
        // include[candidates][]=candidate-assessments&include[candidates][]=candidate-assessments.assessment-template
        Set<String> included = Sets.newHashSet("candidate-assessments", "candidate-assessments.assessment-template");
        queryParams.put("include[candidates]", included);

        // WHEN
        Map<String, Set<String>> result = parser.parseIncludedRelationsParameters(queryParams);

        // THEN
        assertThat(result.entrySet().size()).isEqualTo(1);

    }
}
