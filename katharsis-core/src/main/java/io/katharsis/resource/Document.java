package io.katharsis.resource;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.resource.internal.DocumentDataDeserializer;

public class Document implements MetaContainer, LinksContainer {

	@JsonInclude(Include.NON_EMPTY)
	@JsonDeserialize(using = DocumentDataDeserializer.class)
	private Object data;

	@JsonInclude(Include.NON_EMPTY)
	private List<Resource> included;

	@JsonInclude(Include.NON_EMPTY)
	private ObjectNode links;

	@JsonInclude(Include.NON_EMPTY)
	private ObjectNode meta;

	@JsonInclude(Include.NON_EMPTY)
	private List<ErrorData> errors;

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

	public void setData(Object data) {
		this.data = data;
	}

	public List<Resource> getIncluded() {
		return included;
	}

	public void setIncluded(List<Resource> includes) {
		this.included = includes;
	}

	public List<ErrorData> getErrors() {
		return errors;
	}

	public void setErrors(List<ErrorData> errors) {
		this.errors = errors;
	}

	@JsonIgnore
	public boolean isMultiple() {
		return data instanceof Collection;
	}

	@JsonIgnore
	public Resource getSingleData() {
		return (Resource) data;
	}

	@Override
	public int hashCode() {
		return Objects.hash(data, errors, included, links, meta);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Document))
			return false;
		Document other = (Document) obj;
		return Objects.equals(data, other.data) && Objects.equals(errors, other.errors) // NOSONAR
				&& Objects.equals(included, other.included) && Objects.equals(meta, other.meta) && Objects.equals(links, other.links);
	}
}