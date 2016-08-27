package io.katharsis.client;

import io.katharsis.client.module.TestExceptionMapper;
import io.katharsis.module.SimpleModule;

public class TestModule extends SimpleModule {

	public TestModule() {
		super("test");
		
		addExceptionMapper(new TestExceptionMapper());
	}

}
