package io.katharsis.jpa.internal.meta.impl;

import java.lang.reflect.Type;

import io.katharsis.jpa.internal.meta.MetaMapType;
import io.katharsis.jpa.internal.meta.MetaType;

public class MetaMapTypeImpl extends MetaTypeImpl implements MetaMapType {

	private MetaType keyType;
	private MetaType valueType;

	public MetaMapTypeImpl(MetaElementImpl parent, Class<?> implClass, Type implType, MetaType keyType,
			MetaType valueType) {
		super(parent, implClass, implType);
		this.keyType = keyType;
		this.valueType = valueType;
	}

	@Override
	public MetaType getKeyType() {
		return keyType;
	}

	@Override
	public MetaType getValueType() {
		return valueType;
	}

	@Override
	public Object fromString(String values) {
		throw new UnsupportedOperationException("no yet implemented");
	}
}
