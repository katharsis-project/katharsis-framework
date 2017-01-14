package io.katharsis.jpa.internal.query;

import io.katharsis.meta.model.MetaAttribute;
import io.katharsis.resource.annotations.JsonApiResource;

@JsonApiResource(type = "metaComputedAttribute")
public class MetaComputedAttribute extends MetaAttribute {

	@Override
	public Object getValue(Object dataObject) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setValue(Object dataObject, Object value) {
		throw new UnsupportedOperationException();
	}

}
