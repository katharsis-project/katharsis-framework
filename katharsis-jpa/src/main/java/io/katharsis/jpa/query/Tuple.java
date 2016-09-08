package io.katharsis.jpa.query;

public interface Tuple {

	public <T> T get(String name, Class<T> clazz);
	
	public <T> T get(int index, Class<T> clazz);
}
