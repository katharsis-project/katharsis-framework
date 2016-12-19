package io.katharsis.jpa.internal.meta.impl;

import java.lang.annotation.Annotation;
import java.util.Collection;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.meta.MetaMapAttribute;
import io.katharsis.jpa.internal.meta.MetaMapType;
import io.katharsis.jpa.internal.meta.MetaType;

public class MetaMapAttributeImpl extends MetaElementImpl implements MetaMapAttribute {

	private MetaMapType mapType;

	private String keyString;

	private MetaAttribute mapAttr;

	public MetaMapAttributeImpl(MetaMapType mapType, MetaAttribute mapAttr, String keyString) {
		// we dont 'want to attach to meta model since
		super(null);

		this.keyString = keyString;
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
		throw new UnsupportedOperationException();
	}

	@Override
	public void setValue(Object dataObject, Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getKey() {
		MetaType keyType = mapType.getKeyType();
		return keyType.fromString(keyString);
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

	@Override
	public boolean isId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Annotation> getAnnotations() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> clazz) {
		throw new UnsupportedOperationException();
	}
}
