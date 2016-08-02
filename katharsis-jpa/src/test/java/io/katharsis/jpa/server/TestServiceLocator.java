package io.katharsis.jpa.server;

import javax.persistence.EntityManager;

import io.katharsis.locator.JsonServiceLocator;

public class TestServiceLocator implements JsonServiceLocator {

	private EntityManager em;

	public TestServiceLocator(EntityManager em) {
		this.em = em;
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