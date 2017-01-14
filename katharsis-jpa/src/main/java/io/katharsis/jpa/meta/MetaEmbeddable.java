package io.katharsis.jpa.meta;

import io.katharsis.resource.annotations.JsonApiResource;

@JsonApiResource(type = "meta/embeddable")
public class MetaEmbeddable extends MetaJpaDataObject {

	@Override
	public Object fromString(String value) {
		throw new UnsupportedOperationException("no yet implemented");
	}
}
