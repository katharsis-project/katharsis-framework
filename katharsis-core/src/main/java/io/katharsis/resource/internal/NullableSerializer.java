package io.katharsis.resource.internal;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import io.katharsis.utils.java.Nullable;

public class NullableSerializer extends JsonSerializer<Nullable<Object>> {

	@Override
	public void serialize(Nullable<Object> value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
		if (value.isPresent()) {
			Object object = value.get();
			if (object == null) {
				gen.writeNull();
			} else {
				gen.writeObject(object);
			}
		}
	}

	@Override
	public boolean isEmpty(SerializerProvider provider, Nullable<Object> value) {
		return !value.isPresent();
	}
}
