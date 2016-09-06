package io.katharsis.jpa.internal.meta;

/**
 * Allows to modifier the behavior of looking up attributes. Used to add
 * "non-existing" virtual attributes to data objects.
 */
public interface MetaAttributeFinder {

	MetaAttribute getAttribute(MetaDataObject meta, String name);

}
