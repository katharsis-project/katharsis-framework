package io.katharsis.jpa.internal.meta;

public interface MetaArrayType extends MetaType {

	@Override
	public MetaType getElementType();

}