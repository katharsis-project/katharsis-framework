package io.katharsis.errorHandling.mapper;

import io.katharsis.errorHandling.exception.KatharsisException;
import io.katharsis.errorHandling.handlers.NoAnnotationExceptionMapper;
import io.katharsis.errorHandling.handlers.SomeExceptionMapper;
import io.katharsis.resource.exception.init.InvalidResourceException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.*;

public class ExceptionMapperRegistryBuilderTest {

    ExceptionMapperRegistryBuilder builder = new ExceptionMapperRegistryBuilder();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldThrowExceptionWhenAnnotatedClassIsNotImplementingJsonMapper() throws Exception {
        expectedException.expect(InvalidResourceException.class);
        builder.build("io.katharsis.errorHandling.badhandler");
    }

    @Test
    public void shouldContainDefaultKatharsisExceptionMapper() throws Exception {
        ExceptionMapperRegistry registry = builder.build("io.katharsis.errorHandling.handlers");
        assertThat(registry.getExceptionMappers())
                .isNotNull()
                .extracting("exceptionClass")
                .contains(KatharsisException.class);
    }

    @Test
    public void shouldContainScannedExceptionMapper() throws Exception {
        ExceptionMapperRegistry registry = builder.build("io.katharsis.errorHandling.handlers");
        assertThat(registry.getExceptionMappers())
                .isNotNull()
                .extracting("exceptionClass")
                .contains(SomeExceptionMapper.SomeException.class);
    }

    @Test
    public void shouldNotContainNotAnnotatedExceptionMapper() throws Exception {
        ExceptionMapperRegistry registry = builder.build("io.katharsis.errorHandling.handlers");
        assertThat(registry.getExceptionMappers())
                .isNotNull()
                .extracting("exceptionClass")
                .doesNotContain(NoAnnotationExceptionMapper.ShouldNotAppearException.class);
    }

}
