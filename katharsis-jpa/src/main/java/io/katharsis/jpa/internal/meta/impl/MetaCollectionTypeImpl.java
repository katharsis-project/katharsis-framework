package io.katharsis.jpa.internal.meta.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.katharsis.jpa.internal.meta.MetaCollectionType;
import io.katharsis.jpa.internal.meta.MetaType;

public class MetaCollectionTypeImpl extends MetaTypeImpl implements MetaCollectionType {

	private MetaType elementType;

	public MetaCollectionTypeImpl(MetaElementImpl parent, Class<?> implClass, Type implType, MetaType elementType) {
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

	@SuppressWarnings("rawtypes")
	@Override
	public Collection<?> newInstance() {
		if (getImplementationClass() == Set.class)
			return new HashSet();
		if (getImplementationClass() == List.class)
			return new ArrayList();
		throw new UnsupportedOperationException(getImplementationClass().getName());
	}

}
