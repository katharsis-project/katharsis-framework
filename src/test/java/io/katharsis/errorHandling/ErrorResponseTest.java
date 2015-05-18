package io.katharsis.errorHandling;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class ErrorResponseTest {

    @Test
    public void shouldFulfillHashcodeEqualsContract() throws Exception {
        EqualsVerifier.forClass(ErrorResponse.class).verify();
    }
}