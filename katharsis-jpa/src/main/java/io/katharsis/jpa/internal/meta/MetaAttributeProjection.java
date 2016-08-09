package io.katharsis.jpa.internal.meta;

public interface MetaAttributeProjection extends MetaAttribute {

	MetaAttributePath getPath();

	public boolean isDerived();

}
