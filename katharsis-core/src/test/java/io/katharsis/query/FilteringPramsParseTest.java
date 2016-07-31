package io.katharsis.query;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test that validates we can parse filtering params correctly.
 */
public class FilteringPramsParseTest {

    String filterUrl3 = "filter[tasks][name][$startWith]=Super task";
    String filterUrl4 = "filter[tasks][name][][$startWith]=Super&filter[tasks][name][][$endWith]=task";

    DefaultQueryParamsParser parser = new DefaultQueryParamsParser();

    @Test
    public void testParseFilterWithOneParam() throws Exception {
        String query = "filter[tasks][name]=Super task";
        QueryParams params = parser.parse(query);

        assertThat(params.getFilters().size()).isEqualTo(1);

        FilterParam filter = params.getFilter("[tasks][name]");
        assertThat(filter).isNotNull();
        assertThat(filter.getValues().contains("Super task")).isTrue();
    }

    @Test
    public void testParseQueryWithTwoFilterParams() throws Exception {
        String query = "filter[tasks][name]=Super task&filter[tasks][dueDate]=2015-10-01";
        QueryParams params = parser.parse(query);

        assertThat(params.getFilters().size()).isEqualTo(2);

        FilterParam filter = params.getFilter("[tasks][name]");
        assertThat(filter).isNotNull();
        assertThat(filter.getValues().contains("Super task")).isTrue();

        filter = params.getFilter("[tasks][dueDate]");
        assertThat(filter).isNotNull();
        assertThat(filter.getValues().contains("2015-10-01")).isTrue();

    }

    @Test
    public void testParseFilterWithMixedCase() throws Exception {
        String query = "filter[tasks][name]=Super task&FilTer[tasks][dueDate]=2015-10-01";
        QueryParams params = parser.parse(query);

        assertThat(params.getFilters().size()).isEqualTo(2);

        FilterParam filter = params.getFilter("[tasks][name]");
        assertThat(filter).isNotNull();
        assertThat(filter.getValues().contains("Super task")).isTrue();

        filter = params.getFilter("[tasks][dueDate]");
        assertThat(filter).isNotNull();
        assertThat(filter.getValues().contains("2015-10-01")).isTrue();
    }

    @Test
    public void testParseFilterWithExtraParameters() throws Exception {
        String query = "filter[tasks][name]=Super task&extra[tasks][dueDate]=2015-10-01";
        QueryParams params = parser.parse(query);

        assertThat(params.getFilters().size()).isEqualTo(1);

        FilterParam filter = params.getFilter("[tasks][name]");
        assertThat(filter).isNotNull();
        assertThat(filter.getValues().contains("Super task")).isTrue();
    }


}
