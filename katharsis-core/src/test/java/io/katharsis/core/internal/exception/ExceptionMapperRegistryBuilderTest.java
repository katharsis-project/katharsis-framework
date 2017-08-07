package io.katharsis.core.internal.exception;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.ErrorResponseBuilder;
import io.katharsis.errorhandling.exception.InvalidResourceException;
import io.katharsis.errorhandling.exception.KatharsisMappableException;
import io.katharsis.errorhandling.handlers.BaseExceptionMapper;
import io.katharsis.errorhandling.handlers.NoAnnotationExceptionMapper;
import io.katharsis.errorhandling.handlers.SomeExceptionMapper;
import io.katharsis.errorhandling.mapper.JsonApiExceptionMapper;
import io.katharsis.legacy.queryParams.errorhandling.ExceptionMapperProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ExceptionMapperRegistryBuilderTest {

    private final ExceptionMapperRegistryBuilder builder = new ExceptionMapperRegistryBuilder();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldThrowExceptionWhenAnnotatedClassIsNotImplementingJsonMapper() throws Exception {
        expectedException.expect(InvalidResourceException.class);
        builder.build("io.katharsis.errorhandling.badhandler");
    }

    @Test
    public void shouldContainDefaultKatharsisExceptionMapper() throws Exception {
        ExceptionMapperRegistry registry = builder.build("io.katharsis.errorhandling.handlers");
        assertThat(registry.getExceptionMappers())
            .isNotNull()
            .extracting("exceptionClass")
            .contains(KatharsisMappableException.class)
            .contains(SomeExceptionMapper.SomeException.class)
            .contains(IllegalArgumentException.class);

        assertThat(registry.getExceptionMappers())
                .extracting("exceptionMapper")
                .extracting("class")
                .contains(KatharsisExceptionMapper.class);
    }

    @Test
    public void shouldNotAddDefaultExceptionMapperIfCustomMapperIsFound() throws Exception {
        ExceptionMapperRegistry registry = builder.build(() -> {
			Set<JsonApiExceptionMapper> mappers = new HashSet<>();
			mappers.add(new CustomKatharsisMappableExceptionMapper());
			return mappers;
		});

        assertThat(registry.getExceptionMappers())
                .extracting("exceptionMapper")
                .extracting("class")
                .contains(CustomKatharsisMappableExceptionMapper.class)
                .doesNotContain(KatharsisExceptionMapper.class);
    }

    @Test
    public void shouldContainScannedExceptionMapper() throws Exception {
        ExceptionMapperRegistry registry = builder.build("io.katharsis.errorhandling.handlers");
        assertThat(registry.getExceptionMappers())
            .isNotNull()
            .extracting("exceptionClass")
            .contains(SomeExceptionMapper.SomeException.class)
            .contains(IllegalArgumentException.class);
    }

    @Test
    public void shouldNotContainNotAnnotatedExceptionMapper() throws Exception {
        ExceptionMapperRegistry registry = builder.build("io.katharsis.errorhandling.handlers");
        assertThat(registry.getExceptionMappers())
            .isNotNull()
            .extracting("exceptionClass")
            .doesNotContain(NoAnnotationExceptionMapper.ShouldNotAppearException.class);
    }


    @Test
    public void shouldContainScannedExceptionMapperWhenMultiplePaths() throws Exception {
        ExceptionMapperRegistry registry = builder.build("io.katharsis.errorhandling.handlers,io.katharsis.errorhandling.handlers");
        assertThat(registry.getExceptionMappers())
            .isNotNull()
            .extracting("exceptionClass")
            .contains(SomeExceptionMapper.SomeException.class)
            .contains(IllegalArgumentException.class);
    }

    @ExceptionMapperProvider
    public static class CustomKatharsisMappableExceptionMapper extends BaseExceptionMapper<KatharsisMappableException> {

        @Override
        public ErrorResponse toErrorResponse(KatharsisMappableException exception) {
            return new ErrorResponseBuilder()
                    .setStatus(500)
                    .setSingleErrorData(ErrorData.builder()
                            .setTitle("Custom Error")
                            .build())
                    .build();
        }
    }

}
