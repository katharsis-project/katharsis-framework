package io.katharsis.resource;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.resource.internal.DocumentDataDeserializer;

public class Document {

	@JsonInclude(Include.NON_EMPTY)
	@JsonDeserialize(using = DocumentDataDeserializer.class)
	private Object data;

	@JsonInclude(Include.NON_EMPTY)
	private List<Resource> includes;

	@JsonInclude(Include.NON_EMPTY)
	private JsonNode links;

	@JsonInclude(Include.NON_EMPTY)
	private JsonNode meta;

	@JsonInclude(Include.NON_EMPTY)
	private List<ErrorData> errors;

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

	public void setData(Object data) {
		this.data = data;
	}

	public List<Resource> getIncludes() {
		return includes;
	}

	public void setIncludes(List<Resource> includes) {
		this.includes = includes;
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
		return Objects.hash(data, errors, includes, links, meta);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Document))
			return false;
		Document other = (Document) obj;
		return Objects.equals(data, other.data) && Objects.equals(errors, other.errors) // NOSONAR
				&& Objects.equals(includes, other.includes) && Objects.equals(meta, other.meta)
				&& Objects.equals(links, other.links);
	}
}