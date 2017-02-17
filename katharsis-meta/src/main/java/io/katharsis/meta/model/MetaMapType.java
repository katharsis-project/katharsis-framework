package io.katharsis.meta.model;

import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToOne;

@JsonApiResource(type = "meta/mapType")
public class MetaMapType extends MetaType {

	@JsonApiToOne
	private MetaType keyType;

	@JsonApiToOne
	private MetaType valueType;

	public MetaType getKeyType() {
		return keyType;
	}

	public MetaType getValueType() {
		return valueType;
	}

	public void setKeyType(MetaType keyType) {
		this.keyType = keyType;
	}

	public void setValueType(MetaType valueType) {
		this.valueType = valueType;
	}
}
