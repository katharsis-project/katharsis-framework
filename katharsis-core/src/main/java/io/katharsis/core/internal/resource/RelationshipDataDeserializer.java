package io.katharsis.core.internal.resource;

import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.katharsis.resource.ResourceIdentifier;
import io.katharsis.utils.Nullable;

public class RelationshipDataDeserializer extends JsonDeserializer<Nullable<Object>> {

	@Override
	public Nullable<Object> deserialize(JsonParser jp, DeserializationContext context) throws IOException, JsonProcessingException {
		JsonToken currentToken = jp.getCurrentToken();
		if (currentToken == JsonToken.START_ARRAY) {
			ResourceIdentifier[] resources = jp.readValueAs(ResourceIdentifier[].class);
			return Nullable.of((Object) Arrays.asList(resources));
		} else if (currentToken == JsonToken.VALUE_NULL) {
			return Nullable.of(null);
		} else if (currentToken == JsonToken.START_OBJECT) {
			return Nullable.of((Object) jp.readValueAs(ResourceIdentifier.class));
		}
		throw new IllegalStateException(currentToken.toString());
	}

	@Override
	public Nullable<Object> getNullValue(DeserializationContext ctxt) throws JsonMappingException {
		return Nullable.nullValue();
	}

}
