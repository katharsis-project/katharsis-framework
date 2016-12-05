package io.katharsis.jpa.internal.meta.impl;

import java.lang.reflect.Type;

import io.katharsis.jpa.internal.meta.MetaCollectionType;
import io.katharsis.jpa.internal.meta.MetaMapType;
import io.katharsis.jpa.internal.meta.MetaType;

public class MetaTypeImpl extends MetaElementImpl implements MetaType {

	private Class<?> implClass;

	private Type implType;

	public MetaTypeImpl(MetaElementImpl parent, Class<?> implClass, Type implType) {
		super(parent);
		this.implClass = implClass;
		this.implType = implType;
	}

	@Override
	public Class<?> getImplementationClass() {
		return implClass;
	}

	@Override
	public Type getImplementationType() {
		return implType;
	}

	@Override
	public String getName() {
		return implClass.getSimpleName();
	}

	@Override
	public String getId() {
		return implClass.getName();
	}

	@Override
	public Object fromString(String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isCollection() {
		return this instanceof MetaCollectionType;
	}

	@Override
	public MetaCollectionType asCollection() {
		return (MetaCollectionType) this;
	}

	@Override
	public boolean isMap() {
		return this instanceof MetaMapType;
	}

	@Override
	public MetaMapType asMap() {
		return (MetaMapType) this;
	}

	@Override
	public MetaType getElementType() {
		if (isCollection()) {
			return asCollection().getElementType();
		}
		else if (isMap()) {
			return asMap().getValueType();
		}
		else {
			return this;
		}
	}
}
