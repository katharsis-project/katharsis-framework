package io.katharsis.resource.internal;

import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import io.katharsis.resource.ResourceId;

public class RelationshipDataDeserializer extends JsonDeserializer<Object> {

	@Override
	public Object deserialize(JsonParser jp, DeserializationContext context) throws IOException, JsonProcessingException {
		JsonToken currentToken = jp.getCurrentToken();
		if (currentToken == JsonToken.START_ARRAY) {
			ResourceId[] resources = jp.readValueAs(ResourceId[].class);
			return Arrays.asList(resources);
		}
		else if (currentToken == JsonToken.VALUE_NULL) {
			return null;
		}
		else if (currentToken == JsonToken.START_OBJECT) {
			return jp.readValueAs(ResourceId.class);
		}
		throw new IllegalStateException(currentToken.toString());
	}

}
