package io.katharsis.jpa.query;

public interface Tuple {

	public <T> T get(String name, Class<T> clazz);
}
