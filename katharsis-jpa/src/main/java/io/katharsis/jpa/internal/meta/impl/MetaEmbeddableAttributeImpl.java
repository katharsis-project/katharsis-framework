package io.katharsis.jpa.internal.meta.impl;

import java.beans.PropertyDescriptor;

public class MetaEmbeddableAttributeImpl extends AbstractMetaEntityAttributeImpl {
	public MetaEmbeddableAttributeImpl(MetaEmbeddableImpl parent, PropertyDescriptor desc) {
		super(parent, desc);
	}
}