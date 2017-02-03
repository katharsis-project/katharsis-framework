package io.katharsis.core.internal.exception;

import org.junit.Test;

import io.katharsis.core.internal.exception.ExceptionMapperType;
import nl.jqno.equalsverifier.EqualsVerifier;

public class ExceptionMapperTypeTest {

    @Test
    public void shouldFulfillHashCodeEqualsContract() throws Exception {
        EqualsVerifier.forClass(ExceptionMapperType.class).verify();
    }
}