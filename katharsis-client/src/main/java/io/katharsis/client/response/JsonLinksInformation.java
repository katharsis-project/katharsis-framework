package io.katharsis.client.response;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.response.LinksInformation;

public class JsonLinksInformation implements LinksInformation {

	private JsonNode data;

	private ObjectMapper mapper;

	public JsonLinksInformation(JsonNode data, ObjectMapper mapper) {
		this.data = data;
		this.mapper = mapper;
	}

	public JsonNode asJsonNode() {
		return data;
	}

	/**
	 * Converts this generic links information to the provided type.
	 * @param linksClass to return
	 * @return links information based on the provided type.
	 */
	public <L extends LinksInformation> L as(Class<L> linksClass) {
		try {
			return mapper.readerFor(linksClass).readValue(data);
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
