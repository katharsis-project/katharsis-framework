package io.katharsis.client.response;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.core.internal.utils.CastableInformation;
import io.katharsis.resource.meta.MetaInformation;

public class JsonMetaInformation implements MetaInformation, CastableInformation<MetaInformation> {

	private JsonNode data;

	private ObjectMapper mapper;

	public JsonMetaInformation(JsonNode data, ObjectMapper mapper) {
		this.data = data;
		this.mapper = mapper;
	}

	public JsonNode asJsonNode() {
		return data;
	}

	/**
	 * Converts this generic meta information to the provided type.
	 * 
	 * @param metaClass to return
	 * @return meta information based on the provided type.
	 */
	@Override
	public <M extends MetaInformation> M as(Class<M> metaClass) {
		try {
			return mapper.readerFor(metaClass).readValue(data);
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
