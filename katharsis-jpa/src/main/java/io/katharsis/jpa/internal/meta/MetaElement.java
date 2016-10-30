package io.katharsis.jpa.internal.meta;

import java.util.List;

public interface MetaElement {

	/**
	 * Initializes the given element. Usually relations to other meta elements
	 * are resolved here to allow cyclic dependencies between elements not
	 * possible within constructors.
	 * 
	 * @param lookup that manages this instance
	 */
	void init(MetaLookup lookup);

	MetaDataObject asDataObject();

	MetaEntity asEntity();

	String getName();

	String getId();

	MetaElement getParent();

	MetaType asType();

	<T extends MetaElement> List<T> getChildren();
}
