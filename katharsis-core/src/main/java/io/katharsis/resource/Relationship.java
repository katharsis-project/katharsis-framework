package io.katharsis.resource;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

public class Relationship {

	private Object data;

	private JsonNode links;

	private JsonNode meta;

	public Relationship() {
	}

	public Relationship(ResourceId resourceId) {
		this.data = resourceId;
	}
	
	public Relationship(List<ResourceId> resourceIds) {
		this.data = resourceIds;
	}

	public JsonNode getLinks() {
		return links;
	}

	public void setLinks(JsonNode links) {
		this.links = links;
	}

	public JsonNode getMeta() {
		return meta;
	}

	public void setMeta(JsonNode meta) {
		this.meta = meta;
	}

	public Object getData() {
		return data;
	}

	@JsonIgnore
	public ResourceId getSingleData() {
		return (ResourceId) data;
	}

	@JsonIgnore
	public List<ResourceId> getCollectionData() {
		return (List<ResourceId>) data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}