package io.katharsis.jpa.internal.meta.impl;

import java.lang.reflect.Type;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaEntity;

public class MetaEntityImpl extends AbstractMetaEntityImpl implements MetaEntity {

	public MetaEntityImpl(Class<?> implClass, Type implType, MetaDataObjectImpl superType) {
		super(implClass, implType, superType);
	}

}