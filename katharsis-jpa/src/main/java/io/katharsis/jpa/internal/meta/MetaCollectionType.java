package io.katharsis.jpa.internal.meta;

import java.util.Collection;

public interface MetaCollectionType extends MetaType {

	public MetaType getElementType();

	public Collection<?> newInstance();

}