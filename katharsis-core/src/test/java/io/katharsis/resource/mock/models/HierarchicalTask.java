package io.katharsis.resource.mock.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiRelation;
import io.katharsis.resource.annotations.JsonApiResource;

@JsonApiResource(type = "hierarchicalTask")
@JsonPropertyOrder(alphabetic = true)
public class HierarchicalTask {

	@JsonApiId
	private Long id;

	private String name;

	@JsonApiRelation(opposite = "children")
	private HierarchicalTask parent;

	@JsonApiRelation(opposite = "parent")
	private List<HierarchicalTask> children;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HierarchicalTask getParent() {
		return parent;
	}

	public void setParent(HierarchicalTask parent) {
		this.parent = parent;
	}

	public List<HierarchicalTask> getChildren() {
		return children;
	}

	public void setChildren(List<HierarchicalTask> children) {
		this.children = children;
	}
}
