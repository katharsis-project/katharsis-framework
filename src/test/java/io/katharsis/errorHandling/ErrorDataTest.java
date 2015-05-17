package io.katharsis.errorHandling;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class ErrorDataTest {

    @Test
    public void shouldFulfillEqualsHashCodeContract() throws Exception {
        EqualsVerifier.forClass(ErrorData.class).verify();
    }

}