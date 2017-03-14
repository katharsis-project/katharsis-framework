package io.katharsis.meta.model;

import io.katharsis.resource.annotations.JsonApiResource;

@JsonApiResource(type = "meta/primaryKey")
public class MetaPrimaryKey extends MetaKey {

	private boolean generated;

	public boolean isGenerated() {
		return generated;
	}

	public void setGenerated(boolean generated) {
		this.generated = generated;
	}
}
