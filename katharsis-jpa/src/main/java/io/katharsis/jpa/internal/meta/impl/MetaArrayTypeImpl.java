package io.katharsis.jpa.internal.meta.impl;

import java.lang.reflect.Type;

import io.katharsis.jpa.internal.meta.MetaArrayType;
import io.katharsis.jpa.internal.meta.MetaType;

public class MetaArrayTypeImpl extends MetaTypeImpl implements MetaArrayType {

	private MetaType elementType;

	public MetaArrayTypeImpl(MetaElementImpl parent, Class<?> implClass, Type implType, MetaType elementType) {
		super(parent, implClass, implType);
		this.elementType = elementType;
	}

	@Override
	public MetaType getElementType() {
		return elementType;
	}

	@Override
	public Object fromString(String values) {
		throw new UnsupportedOperationException("no yet implemented");
	}
}
