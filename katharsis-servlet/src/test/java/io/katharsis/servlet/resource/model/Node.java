package io.katharsis.servlet.resource.model;

import java.util.Set;

import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToMany;
import io.katharsis.resource.annotations.JsonApiToOne;


@JsonApiResource(type = "nodes")
public class Node extends AbstractResource {

	@JsonApiToOne
	private Node parent;

	@JsonApiToMany
	private Set<Node> children;

	@JsonApiToMany
	private Set<NodeComment> nodeComments;

	public Node(Long id, Node parent, Set<Node> children) {
		super(id);
		this.parent = parent;
		this.children = children;
	}

	public Node(Long id, Node parent, Set<Node> children, Set<NodeComment> nodeComments) {
		super(id);
		this.parent = parent;
		this.children = children;
		this.nodeComments = nodeComments;
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

	public Set<NodeComment> getNodeComments() {
		return nodeComments;
	}

	public void setNodeComments(Set<NodeComment> nodeComments) {
		this.nodeComments = nodeComments;
	}
}
