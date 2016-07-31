package io.katharsis.query;


import io.katharsis.errorhandling.exception.QueryParseException;
import io.katharsis.queryParams.PaginationKey;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class PaginationParamTest {

    @Test
    public void testPaginationWithSize() throws Exception {
        PaginationParam param = PaginationParam.build("page[size]", Collections.singleton("3"));

        assertThat(param.getPaginationKey()).isEqualTo(PaginationKey.size);
        assertThat(param.getAsLong()).isEqualTo(3);
    }

    @Test
    public void testPaginationWithoutValueThrowsException() throws Exception {
        try {
            PaginationParam param = PaginationParam.build("page[size]", Collections.<String>emptyList());
        } catch (Exception e) {
            assertThat(e instanceof QueryParseException).isTrue();
        }
    }

}