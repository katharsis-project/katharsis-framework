package io.katharsis.jpa.meta;

import io.katharsis.jpa.query.AnyTypeObject;
import io.katharsis.meta.model.MetaAttribute;
import io.katharsis.resource.annotations.JsonApiResource;

@JsonApiResource(type = "meta/embeddableAttribute")
public class MetaEmbeddableAttribute extends MetaAttribute {

	private static final Object VALUE_ANYTYPE_ATTR_NAME = "value";

	@Override
	public boolean isDerived() {
		return super.isDerived() || AnyTypeObject.class.isAssignableFrom(getParent().getImplementationClass()) && getName().equals(VALUE_ANYTYPE_ATTR_NAME);
	}
}