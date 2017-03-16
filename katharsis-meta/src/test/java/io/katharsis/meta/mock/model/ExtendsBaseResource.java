package io.katharsis.meta.mock.model;

import io.katharsis.resource.annotations.JsonApiResource;

@JsonApiResource(type = "extendsBase")
public class ExtendsBaseResource extends BaseObject {

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
