package io.katharsis.resource.mock.models;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiResource;

@JsonApiResource(type = "schedules")
public class Schedule {

	@JsonApiId
	private Long id;

	private String name;
	
	public Long getId() {
		return id;
	}

	public Schedule setId(Long id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
