package io.katharsis.jpa.internal.meta;

// dyn beans?
public interface MetaMapAttribute extends MetaAttribute {

	public boolean isKeyAccess();

	public Object getKey();

	public MetaAttribute getMapAttribute();
}
