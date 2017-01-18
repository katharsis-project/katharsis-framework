package io.katharsis.meta.model.resource;

import java.util.List;

import io.katharsis.resource.annotations.JsonApiResource;

@JsonApiResource(type = "meta/resource")
public class MetaResource extends MetaJsonObject {

	private String resourceType;

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getResourceType() {
		return resourceType;
	}

	@Override
	public List<MetaResourceField> getDeclaredAttributes() {
		return (List<MetaResourceField>) super.getDeclaredAttributes();
	}
}
