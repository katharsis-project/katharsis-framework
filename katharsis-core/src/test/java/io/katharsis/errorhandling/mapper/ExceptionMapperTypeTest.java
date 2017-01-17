package io.katharsis.errorhandling.mapper;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class ExceptionMapperTypeTest {

    @Test
    public void shouldFulfillHashCodeEqualsContract() throws Exception {
        EqualsVerifier.forClass(ExceptionMapperType.class).verify();
    }
}