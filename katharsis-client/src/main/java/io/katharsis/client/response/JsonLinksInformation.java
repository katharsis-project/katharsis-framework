package io.katharsis.client.response;

import com.fasterxml.jackson.databind.JsonNode;

import io.katharsis.response.LinksInformation;

public class JsonLinksInformation implements LinksInformation {

	private JsonNode data;

	public JsonLinksInformation(JsonNode data) {
		this.data = data;
	}

	public JsonNode asJsonNode() {
		return data;
	}
}
