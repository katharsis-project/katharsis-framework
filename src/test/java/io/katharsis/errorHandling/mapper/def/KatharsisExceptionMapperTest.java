package io.katharsis.errorHandling.mapper.def;

import io.katharsis.errorHandling.ErrorData;
import io.katharsis.errorHandling.ErrorResponse;
import io.katharsis.errorHandling.exception.KatharsisException;
import io.katharsis.response.HttpStatus;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class KatharsisExceptionMapperTest {

    public static final String TITLE1 = "title1";
    public static final String DETAIL1 = "detail1";

    @Test
    public void shouldMapToErrorResponse() throws Exception {
        KatharsisExceptionMapper mapper = new KatharsisExceptionMapper();
        ErrorResponse response = mapper.toErrorResponse(new SampleKatharsisException());

        assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        assertThat(response.getData())
                .hasSize(1)
                .extracting("title", "detail")
                .containsExactly(tuple(TITLE1, DETAIL1));
    }

    private static class SampleKatharsisException extends KatharsisException {

        protected SampleKatharsisException() {
            super(HttpStatus.INTERNAL_SERVER_ERROR_500, ErrorData.builder()
                    .setTitle(TITLE1)
                    .setDetail(DETAIL1)
                    .setStatus(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR_500))
                    .build());
        }
    }
}