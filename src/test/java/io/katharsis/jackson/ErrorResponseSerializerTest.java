package io.katharsis.jackson;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.ErrorResponseBuilder;

import org.junit.Test;

public class ErrorResponseSerializerTest extends BaseSerializerTest {

    @Test
    public void onErrorData() throws Exception {
        // WHEN
        ErrorData errorData = ErrorData.builder().setId("error1").setCode("CODE1").setTitle("hello").setDetail("world").build();
        ErrorResponse errorResponse = new ErrorResponseBuilder().setStatus(500)
            .setSingleErrorData(errorData).build();
        String result = sut.writeValueAsString(errorResponse);

        // THEN
        assertThatJson(result).node("errors").isPresent();
        assertThatJson(result).node("errors[0].id").isEqualTo("\"error1\"");
        assertThatJson(result).node("errors[0].code").isEqualTo("\"CODE1\"");
        assertThatJson(result).node("errors[0].title").isEqualTo("\"hello\"");
        assertThatJson(result).node("errors[0].detail").isEqualTo("\"world\"");
    }

}
