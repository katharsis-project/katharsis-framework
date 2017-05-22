package io.katharsis.queryspec.repository;

import org.junit.Before;

public class LegacyQuerySpecDeserializerTest extends DefaultQuerySpecDeserializerTestBase {

	@Before
	public void setup() {
		super.setup();
		deserializer.setEnforceDotPathSeparator(false);
	}
}
