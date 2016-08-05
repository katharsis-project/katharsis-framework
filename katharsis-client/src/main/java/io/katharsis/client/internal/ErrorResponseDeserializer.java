package io.katharsis.client.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.ErrorResponse;

/**
 * Serializes top-level Errors object.
 */
public class ErrorResponseDeserializer extends JsonDeserializer<ErrorResponse> {

	private static final String LINKS = "links";
	private static final String ID = "id";
	private static final String ABOUT_LINK = "about";
	private static final String STATUS = "status";
	private static final String CODE = "code";
	private static final String TITLE = "title";
	private static final String DETAIL = "detail";
	private static final String SOURCE = "source";
	private static final String POINTER = "pointer";
	private static final String PARAMETER = "parameter";
	private static final String META = "meta";

	@Override
	public ErrorResponse deserialize(JsonParser jp, DeserializationContext context)
			throws IOException, JsonProcessingException {

		JsonNode node = jp.readValueAsTree();
		if (node == null) {
			return null;
		}

		JsonNode errorsNode = node.get(ErrorResponse.ERRORS);
		List<ErrorData> errors = new ArrayList<ErrorData>();
		if (errorsNode != null) {
			if (!errorsNode.isArray()) {
				throw new RuntimeException("data field has wrong type: " + node.toString());
			}
			Iterator<JsonNode> iterator = errorsNode.elements();
			while (iterator.hasNext()) {
				JsonNode errorNode = iterator.next();
				String id = readStringIfExists(ID, errorNode);
				String aboutLink = readAboutLink(errorNode);
				String status = readStringIfExists(STATUS, errorNode);
				String code = readStringIfExists(CODE, errorNode);
				String title = readStringIfExists(TITLE, errorNode);
				String detail = readStringIfExists(DETAIL, errorNode);
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
		JsonNode metaNode = errorNode.get(META);
		if (metaNode != null) {
			return jp.getCodec().treeToValue(metaNode, Map.class);
		}
		return null;
	}

	private static String readSourcePointer(JsonNode errorNode) throws IOException {
		JsonNode node = errorNode.get(SOURCE);
		if (node != null) {
			return readStringIfExists(POINTER, node);
		}
		return null;
	}

	private static String readSourceParameter(JsonNode errorNode) throws IOException {
		JsonNode node = errorNode.get(SOURCE);
		if (node != null) {
			return readStringIfExists(PARAMETER, node);
		}
		return null;
	}

	private static String readAboutLink(JsonNode errorNode) throws IOException {
		JsonNode node = errorNode.get(LINKS);
		if (node != null) {
			return readStringIfExists(ABOUT_LINK, node);
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

	public Class<ErrorResponse> handledType() {
		return ErrorResponse.class;
	}
}
