package io.katharsis.meta.model;

import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToOne;

@JsonApiResource(type = "meta/arrayType")
public class MetaArrayType extends MetaType {

	@JsonApiToOne
	private MetaType elementType;

	public void setElementType(MetaType elementType) {
		this.elementType = elementType;
	}

	@Override
	public MetaType getElementType() {
		return elementType;
	}
}
