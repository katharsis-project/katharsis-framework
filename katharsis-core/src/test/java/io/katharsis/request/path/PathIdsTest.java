package io.katharsis.request.path;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class PathIdsTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(PathIds.class)
                .usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }
}
