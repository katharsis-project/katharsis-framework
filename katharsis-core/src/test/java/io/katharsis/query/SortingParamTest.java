package io.katharsis.query;

import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class SortingParamTest {

    @Test
    public void testSingleParamIsParsedAndDefaultsToAscending() throws Exception {
        // GET /people?sort=age HTTP/1.1
        SortingParam sortingParam = SortingParam.build(Collections.singleton("age"));

        assertThat(sortingParam.sortFields().size()).isEqualTo(1);
        assertThat(sortingParam.isAscending("age")).isTrue();
    }

    @Test
    public void testMultipleSortParamsAreParsedOk() throws Exception {
        // GET /people?sort=age,name HTTP/1.1
        SortingParam sortingParam = SortingParam.build(Collections.singleton("age,name"));

        assertThat(sortingParam.sortFields().size()).isEqualTo(2);

        assertThat(sortingParam.isAscending("age")).isTrue();
        assertThat(sortingParam.isAscending("name")).isTrue();

        // first param is age
        assertThat(sortingParam.sortEntries().iterator().next().getKey()).isEqualTo("age");
    }

    @Test
    public void testMultipleSortParamsWithDescendingOrder() throws Exception {
        // GET /articles?sort=-created,title HTTP/1.1
        SortingParam sortingParam = SortingParam.build(Collections.singleton("-created,title"));

        assertThat(sortingParam.sortFields().size()).isEqualTo(2);

        assertThat(sortingParam.isAscending("created")).isFalse();
        assertThat(sortingParam.isAscending("title")).isTrue();

        // first param is age
        assertThat(sortingParam.sortEntries().iterator().next().getKey()).isEqualTo("created");
    }

}