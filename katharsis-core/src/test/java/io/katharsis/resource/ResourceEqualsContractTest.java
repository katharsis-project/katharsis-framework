package io.katharsis.resource;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class ResourceEqualsContractTest {

	@Test
	public void testResourceIdEqualsContract() throws NoSuchFieldException {
		EqualsVerifier.forClass(ResourceIdentifier.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
