package io.katharsis.servlet.resource.model;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiIncludeByDefault;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToOne;

/**
 * Created by nickmitchell on 1/6/17.
 */
@JsonApiResource(type = "node-comments")
public class NodeComment {

	@JsonApiId
	private Long id;

	private String comment;

	@JsonApiToOne
	private Node parent;

	@JsonApiToOne
	@JsonApiIncludeByDefault
	private Locale langLocale;

	public NodeComment(Long id, String comment, Node parent, Locale langLocale) {
		this.id = id;
		this.comment = comment;
		this.parent = parent;
		this.langLocale = langLocale;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public Locale getLangLocale() {
		return langLocale;
	}

	public void setLangLocale(Locale langLocale) {
		this.langLocale = langLocale;
	}
}
