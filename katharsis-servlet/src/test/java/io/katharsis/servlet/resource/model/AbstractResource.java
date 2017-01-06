package io.katharsis.servlet.resource.model;

import java.util.Objects;

import io.katharsis.resource.annotations.JsonApiId;

public class AbstractResource {

	@JsonApiId
	private Long id;

	public AbstractResource(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || this.getClass() != o.getClass()) return false;
		AbstractResource that = (AbstractResource) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
