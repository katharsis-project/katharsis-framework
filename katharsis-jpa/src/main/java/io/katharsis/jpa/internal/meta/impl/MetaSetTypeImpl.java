package io.katharsis.jpa.internal.meta.impl;

import java.lang.reflect.Type;

import io.katharsis.jpa.internal.meta.MetaSetType;
import io.katharsis.jpa.internal.meta.MetaType;

public class MetaSetTypeImpl extends MetaCollectionTypeImpl implements MetaSetType {

	public MetaSetTypeImpl(MetaElementImpl parent, Class<?> implClass, Type implType, MetaType elementType) {
		super(parent, implClass, implType, elementType);
	}
}
