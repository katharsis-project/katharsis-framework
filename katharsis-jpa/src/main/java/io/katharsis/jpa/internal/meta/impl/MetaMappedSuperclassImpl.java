package io.katharsis.jpa.internal.meta.impl;

import java.lang.reflect.Type;

public class MetaMappedSuperclassImpl extends AbstractMetaEntityImpl{

	public MetaMappedSuperclassImpl(Class<?> implClass, Type implType, MetaDataObjectImpl superType) {
		super(implClass, implType, superType);
	}
		
}
