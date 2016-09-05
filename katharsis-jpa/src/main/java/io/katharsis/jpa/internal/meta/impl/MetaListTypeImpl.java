package io.katharsis.jpa.internal.meta.impl;

import java.lang.reflect.Type;

import io.katharsis.jpa.internal.meta.MetaListType;
import io.katharsis.jpa.internal.meta.MetaType;

public class MetaListTypeImpl extends MetaCollectionTypeImpl implements MetaListType {

	public MetaListTypeImpl(MetaElementImpl parent, Class<?> implClass, Type implType, MetaType elementType) {
		super(parent, implClass, implType, elementType);
	}
}
