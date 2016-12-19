package io.katharsis.errorhandling;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class ErrorResponseTest {

    @Test
    public void shouldFulfillHashcodeEqualsContract() throws Exception {
        EqualsVerifier.forClass(ErrorResponse.class).verify();
    }
}