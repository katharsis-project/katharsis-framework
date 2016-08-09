package io.katharsis.jpa.internal.meta;

public interface MetaAttribute extends MetaElement {

	public MetaAttribute getOppositeAttribute();

	MetaType getType();

	Object getValue(Object dataObject);

	void setValue(Object dataObject, Object value);

	boolean isAssociation();

	boolean isDerived();

	@Override
	MetaDataObject getParent();

	void addValue(Object dataObject, Object value);

	void removeValue(Object dataObject, Object value);

	public boolean isLazy();

	public boolean isVersion();

}
