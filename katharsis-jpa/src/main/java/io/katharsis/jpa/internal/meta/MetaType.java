package io.katharsis.jpa.internal.meta;

import java.lang.reflect.Type;

public interface MetaType extends MetaElement {

	Class<?> getImplementationClass();

	Type getImplementationType();

	Object fromString(String value);

	public boolean isCollection();

	public MetaCollectionType asCollection();

	public boolean isMap();

	public MetaMapType asMap();

	public MetaType getElementType();

}
