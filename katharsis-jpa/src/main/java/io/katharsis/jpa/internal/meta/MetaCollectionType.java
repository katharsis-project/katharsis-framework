package io.katharsis.jpa.internal.meta;

import java.util.Collection;

public interface MetaCollectionType extends MetaType {

	public <T> Collection<T> newInstance();

}