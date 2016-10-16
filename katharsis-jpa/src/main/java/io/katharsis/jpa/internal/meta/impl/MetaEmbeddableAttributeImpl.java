package io.katharsis.jpa.internal.meta.impl;

import java.beans.PropertyDescriptor;

import io.katharsis.jpa.query.AnyTypeObject;

public class MetaEmbeddableAttributeImpl extends AbstractMetaEntityAttributeImpl {

	private static final Object VALUE_ANYTYPE_ATTR_NAME = "value";

	public MetaEmbeddableAttributeImpl(MetaEmbeddableImpl parent, PropertyDescriptor desc) {
		super(parent, desc);
	}

	@Override
	public boolean isDerived() {
		return super.isDerived() || AnyTypeObject.class.isAssignableFrom(getParent().getImplementationClass())
				&& getName().equals(VALUE_ANYTYPE_ATTR_NAME);
	}
}