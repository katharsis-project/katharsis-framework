package io.katharsis.jpa.internal.meta;

import java.util.List;

public interface MetaKey extends MetaTypedElement {

	List<MetaAttribute> getElements();

	boolean isUnique();

	boolean isPrimaryKey();

	MetaAttribute getUniqueElement();

	/**
	 * Converts a string to a key
	 */
	Object fromKeyString(String idString);

	/**
	 * Converts a key to a string
	 */
	String toKeyString(Object id);

}
