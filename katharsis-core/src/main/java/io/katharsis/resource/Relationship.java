package io.katharsis.resource;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.katharsis.resource.internal.RelationshipDataDeserializer;

public class Relationship implements MetaContainer, LinksContainer {

	@JsonDeserialize(using = RelationshipDataDeserializer.class)
	private Object data;

	private ObjectNode links;

	private ObjectNode meta;

	public Relationship() {
	}

	public Relationship(ResourceId resourceId) {
		this.data = resourceId;
	}

	public Relationship(List<ResourceId> resourceIds) {
		this.data = resourceIds;
	}

	@Override
	public ObjectNode getLinks() {
		return links;
	}

	@Override
	public void setLinks(ObjectNode links) {
		this.links = links;
	}

	@Override
	public ObjectNode getMeta() {
		return meta;
	}

	@Override
	public void setMeta(ObjectNode meta) {
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
		if(data == null){
			return null;
		}
		if (!(data instanceof Iterable)) {
			return Collections.singletonList(getSingleData());
		}
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