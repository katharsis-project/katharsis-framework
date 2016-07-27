package io.katharsis.errorhandling.mapper;

import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.response.HttpStatus;
import io.katharsis.utils.java.Optional;
import org.junit.Test;

import java.nio.file.ClosedFileSystemException;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ExceptionMapperRegistryTest {

    //Reused in RequestDispatcherTest
    public static final ExceptionMapperRegistry exceptionMapperRegistry = new ExceptionMapperRegistry(exceptionMapperTypeSet());

    @Test
    public void shouldReturnIntegerMAXForNotRelatedClasses() {
        int distance = exceptionMapperRegistry.getDistanceBetweenExceptions(Exception.class, SomeException.class);
        assertThat(distance).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    public void shouldReturn0DistanceBetweenSameClass() throws Exception {
        int distance = exceptionMapperRegistry.getDistanceBetweenExceptions(Exception.class, Exception.class);
        assertThat(distance).isEqualTo(0);
    }

    @Test
    public void shouldReturn1AsADistanceBetweenSameClass() throws Exception {
        int distance = exceptionMapperRegistry.getDistanceBetweenExceptions(SomeException.class, Exception.class);
        assertThat(distance).isEqualTo(1);
    }

    @Test
    public void shouldNotFindMapperIfSuperClassIsNotMapped() throws Exception {
        Optional<JsonApiExceptionMapper> mapper = exceptionMapperRegistry.findMapperFor(RuntimeException.class);
        assertThat(mapper.isPresent()).isFalse();
    }

    @Test
    public void shouldFindDirectExceptionMapper() throws Exception {
        Optional<JsonApiExceptionMapper> mapper = exceptionMapperRegistry.findMapperFor(IllegalStateException.class);
        assertThat(mapper.isPresent()).isTrue();
        assertThat(mapper.get()).isExactlyInstanceOf(IllegalStateExceptionMapper.class);
    }

    @Test
    public void shouldFindDescendantExceptionMapper() throws Exception {
        Optional<JsonApiExceptionMapper> mapper = exceptionMapperRegistry.findMapperFor(ClosedFileSystemException.class);
        assertThat(mapper.isPresent()).isTrue();
        assertThat(mapper.get()).isExactlyInstanceOf(IllegalStateExceptionMapper.class);
    }

    private static class SomeException extends Exception {
    }

    private static Set<ExceptionMapperType> exceptionMapperTypeSet() {
        Set<ExceptionMapperType> types = new HashSet<>();
        types.add(new ExceptionMapperType(IllegalStateException.class, new IllegalStateExceptionMapper()));
        return types;
    }

    public static class IllegalStateExceptionMapper implements JsonApiExceptionMapper<IllegalStateException> {
        @Override
        public ErrorResponse toErrorResponse(IllegalStateException exception) {
            return ErrorResponse.builder().setStatus(HttpStatus.BAD_REQUEST_400).build();
        }
    }
}
