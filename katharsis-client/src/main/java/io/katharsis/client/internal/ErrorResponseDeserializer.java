package io.katharsis.client.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import io.katharsis.client.ResponseBodyException;
import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.jackson.serializer.ErrorResponseSerializer;

/**
 * Serializes top-level Errors object.
 */
public class ErrorResponseDeserializer extends JsonDeserializer<ErrorResponse> {

	@Override
	public ErrorResponse deserialize(JsonParser jp, DeserializationContext context)
			throws IOException {

		JsonNode node = jp.readValueAsTree();
		if (node == null) {
			return null;
		}

		JsonNode errorsNode = node.get(ErrorResponse.ERRORS);
		List<ErrorData> errors = new ArrayList<>();
		if (errorsNode != null) {
			if (!errorsNode.isArray()) {
				throw new ResponseBodyException("data field has wrong type: " + node.toString());
			}
			Iterator<JsonNode> iterator = errorsNode.elements();
			while (iterator.hasNext()) {
				JsonNode errorNode = iterator.next();
				String id = readStringIfExists(ErrorResponseSerializer.ID, errorNode);
				String aboutLink = readAboutLink(errorNode);
				String status = readStringIfExists(ErrorResponseSerializer.STATUS, errorNode);
				String code = readStringIfExists(ErrorResponseSerializer.CODE, errorNode);
				String title = readStringIfExists(ErrorResponseSerializer.TITLE, errorNode);
				String detail = readStringIfExists(ErrorResponseSerializer.DETAIL, errorNode);
				Map<String, Object> meta = readMeta(errorNode, jp);
				String sourcePointer = readSourcePointer(errorNode);
				String sourceParameter = readSourceParameter(errorNode);
				ErrorData error = new ErrorData(id, aboutLink, status, code, title, detail, sourcePointer, sourceParameter, meta);
				errors.add(error);
			}
		}

		int httpStatus = -1; // later
		return new ErrorResponse(errors, httpStatus);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> readMeta(JsonNode errorNode, JsonParser jp) throws IOException {
		JsonNode metaNode = errorNode.get(ErrorResponseSerializer.META);
		if (metaNode != null) {
			return jp.getCodec().treeToValue(metaNode, Map.class);
		}
		return null;
	}

	private static String readSourcePointer(JsonNode errorNode) throws IOException {
		JsonNode node = errorNode.get(ErrorResponseSerializer.SOURCE);
		if (node != null) {
			return readStringIfExists(ErrorResponseSerializer.POINTER, node);
		}
		return null;
	}

	private static String readSourceParameter(JsonNode errorNode) throws IOException {
		JsonNode node = errorNode.get(ErrorResponseSerializer.SOURCE);
		if (node != null) {
			return readStringIfExists(ErrorResponseSerializer.PARAMETER, node);
		}
		return null;
	}

	private static String readAboutLink(JsonNode errorNode) throws IOException {
		JsonNode node = errorNode.get(ErrorResponseSerializer.LINKS);
		if (node != null) {
			return readStringIfExists(ErrorResponseSerializer.ABOUT_LINK, node);
		}
		return null;
	}

	private static String readStringIfExists(String fieldName, JsonNode errorNode) throws IOException {
		JsonNode node = errorNode.get(fieldName);
		if (node != null) {
			return node.asText();
		} else {
			return null;
		}
	}

	@Override
	public Class<ErrorResponse> handledType() {
		return ErrorResponse.class;
	}
}
