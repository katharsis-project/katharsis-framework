package io.katharsis.jpa.query;

public interface Tuple {

	public <T> T get(String name, Class<T> clazz);

	public <T> T get(int index, Class<T> clazz);

	/**
	 * Ignores the given number of entries by incrementing any index access accordingly.
	 * 
	 * @param numEntriesToIgnore
	 */
	public void reduce(int numEntriesToIgnore);
}
