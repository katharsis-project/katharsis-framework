package io.katharsis.errorhandling.mapper;

import io.katharsis.errorhandling.exception.KatharsisMappableException;
import io.katharsis.errorhandling.handlers.ExtendedExceptionMapper;
import io.katharsis.errorhandling.handlers.NoAnnotationExceptionMapper;
import io.katharsis.errorhandling.handlers.SomeExceptionMapper;
import io.katharsis.resource.exception.init.InvalidResourceException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
            .contains(KatharsisMappableException.class);
    }

    @Test
    public void shouldContainScannedExceptionMapper() throws Exception {
        ExceptionMapperRegistry registry = builder.build("io.katharsis.errorhandling.handlers");
        assertThat(registry.getExceptionMappers())
                .isNotNull()
                .extracting("exceptionClass")
                .contains(SomeExceptionMapper.SomeException.class);
    }

    @Test
    public void shouldContainScannedExtendedExceptionMapper() throws Exception {
        ExceptionMapperRegistry registry = builder.build("io.katharsis.errorhandling.handlers");
        assertThat(registry.getExceptionMappers())
                .isNotNull()
                .extracting("exceptionClass")
                .contains(ExtendedExceptionMapper.SomeException.class);
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
                .contains(SomeExceptionMapper.SomeException.class);
    }

}
