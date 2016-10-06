package io.katharsis.jpa.internal.meta.impl;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaAttributePath;
import io.katharsis.jpa.internal.meta.MetaAttributeProjection;
import io.katharsis.jpa.internal.meta.MetaMapAttribute;
import io.katharsis.jpa.internal.meta.MetaMapType;

public class MetaMapAttributeProjectionImpl extends MetaMapAttributeImpl implements MetaMapAttribute, MetaAttributeProjection {

	public MetaMapAttributeProjectionImpl(MetaMapType mapType, MetaAttribute mapAttr, String key, boolean valueAccess) {
		super(mapType, mapAttr, key, valueAccess);
	}

	@Override
	public MetaAttributePath getPath() {
		return ((MetaAttributeProjection) getMapAttribute()).getPath();
	}

	@Override
	public boolean isDerived() {
		return ((MetaAttributeProjection) getMapAttribute()).isDerived();
	}
}
