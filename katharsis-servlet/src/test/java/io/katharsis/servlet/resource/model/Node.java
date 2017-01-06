package io.katharsis.servlet.resource.model;

import java.util.Set;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToMany;
import io.katharsis.resource.annotations.JsonApiToOne;

/**
 * Created by nickmitchell on 1/5/17.
 */
@JsonApiResource(type = "nodes")
public class Node {
	@JsonApiId
	private Long id;

	@JsonApiToOne
	private Node parent;

	@JsonApiToMany
	private Set<Node> children;

	public Node(Long id, Node parent, Set<Node> children) {
		this.id = id;
		this.parent = parent;
		this.children = children;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public Set<Node> getChildren() {
		return children;
	}

	public void setChildren(Set<Node> children) {
		this.children = children;
	}
}
