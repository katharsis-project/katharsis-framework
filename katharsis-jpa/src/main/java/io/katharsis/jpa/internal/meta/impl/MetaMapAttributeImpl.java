package io.katharsis.jpa.internal.meta.impl;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.meta.MetaMapAttribute;
import io.katharsis.jpa.internal.meta.MetaMapType;
import io.katharsis.jpa.internal.meta.MetaType;

public class MetaMapAttributeImpl extends MetaElementImpl implements MetaMapAttribute {

	private boolean valueAccess;
	private MetaMapType mapType;
	private String key;
	private MetaAttribute mapAttr;

	public MetaMapAttributeImpl(MetaMapType mapType, MetaAttribute mapAttr, String key, boolean valueAccess) {
		// we dont 'want to attach to meta model since
		super(null);

		this.key = key; // FIXME convert String!
		this.valueAccess = valueAccess;
		this.mapType = mapType;
		this.mapAttr = mapAttr;
	}

	@Override
	public MetaDataObject getParent() {
		return (MetaDataObject) super.getParent();
	}

	@Override
	public MetaType getType() {
		return mapType;
	}

	@Override
	public Object getValue(Object dataObject) {
		throw new IllegalStateException("not implemented");
	}

	@Override
	public void setValue(Object dataObject, Object value) {
		throw new IllegalStateException("not implemented");
	}

	@Override
	public String getId() {
		throw new IllegalStateException("not implemented");
	}

	@Override
	public Object getKey() {
		return key;
	}

	@Override
	public MetaAttribute getMapAttribute() {
		return mapAttr;
	}

	@Override
	public String getName() {
		return mapAttr.getName();
	}

	@Override
	public boolean isKeyAccess() {
		return !valueAccess;
	}

	@Override
	public boolean isAssociation() {
		return mapAttr.isAssociation();
	}

	@Override
	public boolean isDerived() {
		return mapAttr.isDerived();
	}

	@Override
	public void addValue(Object dataObject, Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeValue(Object dataObject, Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isLazy() {
		return mapAttr.isLazy();
	}

	@Override
	public MetaAttribute getOppositeAttribute() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isVersion() {
		throw new UnsupportedOperationException();
	}
}
