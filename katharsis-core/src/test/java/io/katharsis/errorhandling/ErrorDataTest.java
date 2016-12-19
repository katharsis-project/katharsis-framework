package io.katharsis.errorhandling;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class ErrorDataTest {

    @Test
    public void shouldFulfillEqualsHashCodeContract() throws Exception {
        EqualsVerifier.forClass(ErrorData.class).allFieldsShouldBeUsed().verify();
    }

}