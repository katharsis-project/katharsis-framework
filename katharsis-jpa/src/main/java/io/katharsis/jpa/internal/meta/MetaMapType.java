package io.katharsis.jpa.internal.meta;

public interface MetaMapType extends MetaType {

	public MetaType getKeyType();

	public MetaType getValueType();
}
