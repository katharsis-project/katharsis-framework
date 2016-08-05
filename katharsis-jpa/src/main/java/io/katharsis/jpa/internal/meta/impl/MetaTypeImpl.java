package io.katharsis.jpa.internal.meta.impl;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import io.katharsis.jpa.internal.meta.MetaCollectionType;
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
	public  Object fromString(String value){
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Object> fromString(Set<String> values) {
		Set<Object> result = new HashSet<Object>();
		for (String value : values) {
			result.add(fromString(value));
		}
		return result;
	}

	@Override
	public boolean isCollection() {
		return this instanceof MetaCollectionType;
	}

	@Override
	public MetaCollectionType asCollection() {
		return (MetaCollectionType) this;
	}
}
