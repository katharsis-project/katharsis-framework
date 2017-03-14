package io.katharsis.meta.model;

import io.katharsis.resource.annotations.JsonApiRelation;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.SerializeType;

@JsonApiResource(type = "meta/mapType")
public class MetaMapType extends MetaType {

	@JsonApiRelation(serialize=SerializeType.LAZY)
	private MetaType keyType;

	public MetaType getKeyType() {
		return keyType;
	}

	public void setKeyType(MetaType keyType) {
		this.keyType = keyType;
	}

}
