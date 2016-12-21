package io.katharsis.resource;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.katharsis.resource.internal.RelationshipDataDeserializer;
import io.katharsis.resource.internal.RelationshipDataSerializer;
import io.katharsis.utils.java.Nullable;

public class Relationship implements MetaContainer, LinksContainer {

	@JsonInclude(Include.NON_EMPTY)
	@JsonSerialize(using = RelationshipDataSerializer.class)
	@JsonDeserialize(using = RelationshipDataDeserializer.class)
	private Nullable<Object> data = Nullable.empty();

	private ObjectNode links;

	private ObjectNode meta;

	public Relationship() {
	}

	public Relationship(ResourceId resourceId) {
		this.data = Nullable.of((Object) resourceId);
	}

	public Relationship(List<ResourceId> resourceIds) {
		this.data = Nullable.of((Object) resourceIds);
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

	public Nullable<Object> getData() {
		return data;
	}

	@JsonIgnore
	public Nullable<ResourceId> getSingleData() {
		return (Nullable<ResourceId>) (Nullable) data;
	}

	@JsonIgnore
	public Nullable<List<ResourceId>> getCollectionData() {
		if (!data.isPresent()) {
			return Nullable.empty();
		}
		Object value = data.get();
		if (!(value instanceof Iterable)) {
			return Nullable.of((Collections.singletonList((ResourceId) value)));
		}
		return Nullable.of((List<ResourceId>) value);
	}

	public void setData(Nullable<Object> data) {
		if (data == null) {
			throw new NullPointerException("make use of Nullable");
		}
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