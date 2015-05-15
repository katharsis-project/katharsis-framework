package io.katharsis.errorHandling;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ExceptionMapperRegistryTest {

    ExceptionMapperRegistry exceptionMapperRegistry = new ExceptionMapperRegistry(null);

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

    private static class SomeException extends Exception {
    }
}
