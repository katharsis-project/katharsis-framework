package io.katharsis.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * “Resource objects” appear in a JSON API document to represent resources.
 * <p/>
 * http://jsonapi.org/format/#document-resource-objects
 */
public class Resource extends ResourceId {

	@JsonInclude(Include.NON_EMPTY)
	private Map<String, JsonNode> attributes = new HashMap<>();

	@JsonInclude(Include.NON_EMPTY)
	private Map<String, Relationship> relationships = new HashMap<>();

	@JsonInclude(Include.NON_EMPTY)
	private Map<String, JsonNode> links = new HashMap<>();

	@JsonInclude(Include.NON_EMPTY)
	private Map<String, JsonNode> meta = new HashMap<>();

	public Map<String, JsonNode> getLinks() {
		return links;
	}

	public void setLinks(Map<String, JsonNode> links) {
		this.links = links;
	}

	public Map<String, JsonNode> getMeta() {
		return meta;
	}

	public void setMeta(Map<String, JsonNode> meta) {
		this.meta = meta;
	}

	public Map<String, JsonNode> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, JsonNode> attributes) {
		this.attributes = attributes;
	}

	public Map<String, Relationship> getRelationships() {
		return relationships;
	}

	public void setRelationships(Map<String, Relationship> relationships) {
		this.relationships = relationships;
	}

	public void setAttribute(String name, JsonNode value) {
		attributes.put(name, value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(attributes, relationships, links, meta);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Resource))
			return false;
		Resource other = (Resource) obj;
		return Objects.equals(attributes, other.attributes) && Objects.equals(relationships, other.relationships)
				&& Objects.equals(meta, other.meta) && Objects.equals(links, other.links);
	}

}