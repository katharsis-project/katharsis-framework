package io.katharsis.jpa.internal.meta;

import java.util.List;

public interface MetaElement {

	MetaDataObject asDataObject();

	MetaEntity asEntity();

	String getName();

	String getId();

	MetaElement getParent();

	MetaType asType();

	List<? extends MetaElement> getChildren();
}
