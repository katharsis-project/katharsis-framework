package io.katharsis.client.response;

import com.fasterxml.jackson.databind.JsonNode;

import io.katharsis.response.MetaInformation;

public class JsonMetaInformation implements MetaInformation {

	private JsonNode data;

	public JsonMetaInformation(JsonNode data) {
		this.data = data;
	}

	public JsonNode asJsonNode() {
		return data;
	}
}
