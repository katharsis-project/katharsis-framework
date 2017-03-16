package io.katharsis.meta.model.resource;

import io.katharsis.resource.annotations.JsonApiResource;

/**
 * A JSON API resource.
 */
@JsonApiResource(type = "meta/resource")
public class MetaResource extends MetaResourceBase {

	private String resourceType;

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getResourceType() {
		return resourceType;
	}
}
