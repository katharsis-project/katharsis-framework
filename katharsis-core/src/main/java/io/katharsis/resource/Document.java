package io.katharsis.resource;

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import io.katharsis.errorhandling.ErrorData;

public class Document {

	private Object data;

	private List<Resource> includes;

	private JsonNode links;

	private JsonNode meta;

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

	public boolean isMultiple() {
		return data instanceof Collection;
	}

	public Resource getSingleData() {
		return (Resource) data;
	}
}