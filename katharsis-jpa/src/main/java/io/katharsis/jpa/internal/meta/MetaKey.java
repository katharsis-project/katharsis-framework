package io.katharsis.jpa.internal.meta;

import java.util.List;

public interface MetaKey extends MetaTypedElement {

	List<MetaAttribute> getElements();

	boolean isUnique();

	boolean isPrimaryKey();

	MetaAttribute getUniqueElement();

	/**
	 * @param idString to conver to
	 * @return Converts a string to a key
	 */
	Object fromKeyString(String idString);

	/**
	 * @param id object to convert
	 * @return Converts a key to a string
	 */
	String toKeyString(Object id);

}
