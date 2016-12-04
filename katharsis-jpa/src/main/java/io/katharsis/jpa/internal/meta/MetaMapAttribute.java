package io.katharsis.jpa.internal.meta;

// dyn beans?
public interface MetaMapAttribute extends MetaAttribute {

	public Object getKey();

	public MetaAttribute getMapAttribute();
}
