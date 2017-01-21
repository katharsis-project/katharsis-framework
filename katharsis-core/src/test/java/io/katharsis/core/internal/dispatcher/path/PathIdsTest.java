package io.katharsis.core.internal.dispatcher.path;

import org.junit.Test;

import io.katharsis.core.internal.dispatcher.path.PathIds;
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
