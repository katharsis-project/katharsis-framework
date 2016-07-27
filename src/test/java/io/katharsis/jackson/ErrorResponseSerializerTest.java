package io.katharsis.jackson;

import io.katharsis.errorhandling.ErrorDataMother;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.response.HttpStatus;
import org.assertj.core.util.Lists;
import org.junit.Test;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;

public class ErrorResponseSerializerTest extends BaseSerializerTest {

    private static final ErrorResponse ERROR_RESPONSE = ErrorResponse.builder()
            .setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500)
            .setSingleErrorData(ErrorDataMother.fullyPopulatedErrorData())
            .build();


    @Test
    public void shouldSerializeId() throws Exception {
        String result = sut.writeValueAsString(ERROR_RESPONSE);
        assertThatJson(result).node("errors[0].id").isStringEqualTo(ErrorDataMother.ID);
    }

    @Test
    public void shouldWrapAboutLink() throws Exception {
        String result = sut.writeValueAsString(ERROR_RESPONSE);
        assertThatJson(result).node("errors[0].links.about").isStringEqualTo(ErrorDataMother.ABOUT_LINK);
    }

    @Test
    public void shouldSerializeStatus() throws Exception {
        String result = sut.writeValueAsString(ERROR_RESPONSE);
        assertThatJson(result).node("errors[0].status").isStringEqualTo(ErrorDataMother.STATUS);
    }

    @Test
    public void shouldSerializeCode() throws Exception {
        String result = sut.writeValueAsString(ERROR_RESPONSE);
        assertThatJson(result).node("errors[0].code").isStringEqualTo(ErrorDataMother.CODE);
    }

    @Test
    public void shouldSerializeTitle() throws Exception {
        String result = sut.writeValueAsString(ERROR_RESPONSE);
        assertThatJson(result).node("errors[0].title").isStringEqualTo(ErrorDataMother.TITLE);
    }

    @Test
    public void shouldSerializeDetail() throws Exception {
        String result = sut.writeValueAsString(ERROR_RESPONSE);
        assertThatJson(result).node("errors[0].detail").isStringEqualTo(ErrorDataMother.DETAIL);
    }

    @Test
    public void shouldWrapPointer() throws Exception {
        String result = sut.writeValueAsString(ERROR_RESPONSE);
        assertThatJson(result).node("errors[0].source.pointer").isStringEqualTo(ErrorDataMother.POINTER);
    }

    @Test
    public void shouldWrapParameter() throws Exception {
        String result = sut.writeValueAsString(ERROR_RESPONSE);
        assertThatJson(result).node("errors[0].source.parameter").isStringEqualTo(ErrorDataMother.PARAMETER);
    }

    @Test
    public void shouldSerializeMeta() throws Exception {
        String result = sut.writeValueAsString(ERROR_RESPONSE);
        assertThatJson(result).node("errors[0].meta.key").isStringEqualTo(ErrorDataMother.META_VALUE);
    }

    @Test
    public void shouldNotSerializeSourceIfItsElementsNotExist() throws Exception {
        ErrorResponse responseWithoutSource = ErrorResponse.builder()
                .setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500)
                .setSingleErrorData(ErrorDataMother.fullyPopulatedErrorDataBuilder()
                        .setSourcePointer(null)
                        .setSourceParameter(null)
                        .build())
                .build();
        String result = sut.writeValueAsString(responseWithoutSource);

        assertThatJson(result).node("errors[0].source").isAbsent();
    }

    @Test
    public void shouldSerializeMultipleErrorDataElements() throws Exception {
        ErrorResponse responseWithoutSource = ErrorResponse.builder()
                .setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500)
                .setErrorData(Lists.newArrayList(ErrorDataMother.fullyPopulatedErrorData(), ErrorDataMother.fullyPopulatedErrorData()))
                .build();
        String result = sut.writeValueAsString(responseWithoutSource);

        assertThatJson(result)
                .node("errors[0]").isPresent()
                .node("errors[1]").isPresent();
    }
}
