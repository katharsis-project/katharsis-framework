package io.katharsis.jpa.server;

import io.katharsis.legacy.locator.JsonServiceLocator;

public class TestServiceLocator implements JsonServiceLocator {

	public TestServiceLocator() {
	}

	@Override
	public <T> T getInstance(Class<T> aClass) {
		try {
			return aClass.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}