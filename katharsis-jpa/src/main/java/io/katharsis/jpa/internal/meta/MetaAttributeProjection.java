package io.katharsis.jpa.internal.meta;

public interface MetaAttributeProjection extends MetaAttribute {

	MetaAttributePath getPath();

	@Override
	public boolean isDerived();

}
