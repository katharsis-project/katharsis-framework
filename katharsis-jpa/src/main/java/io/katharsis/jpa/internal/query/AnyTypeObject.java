package io.katharsis.jpa.internal.query;

public interface AnyTypeObject {

	/**
	 * @return the name of the type of the currently set attribute.
	 */
	public String getType();

	/**
	 * @return the value of this anytype.
	 */
	public Object getValue();

	/**
	 * @return the value of this anytype cast to the desired class.
	 */
	public <T> T getValue(Class<T> clazz);

	/**
	 * Sets the value of this anytype
	 * 
	 * @param value
	 *            the new value
	 */
	public void setValue(Object value);

}
