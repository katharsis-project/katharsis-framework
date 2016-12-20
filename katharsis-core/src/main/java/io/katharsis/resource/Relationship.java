package io.katharsis.resource;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.katharsis.resource.internal.RelationshipDataDeserializer;

public class Relationship {

	@JsonDeserialize(using = RelationshipDataDeserializer.class)
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

	@Override
	public int hashCode() {
		return Objects.hash(data, links, meta);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Relationship))
			return false;
		Relationship other = (Relationship) obj;
		return Objects.equals(data, other.data) // NOSONAR
				&& Objects.equals(meta, other.meta) && Objects.equals(links, other.links);
	}
}