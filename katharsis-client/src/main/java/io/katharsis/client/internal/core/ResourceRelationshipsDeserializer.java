package io.katharsis.client.internal.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import io.katharsis.jackson.exception.ParametersDeserializationException;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Deserialize ResourceLinks field which can contain either a list of {@link LinkageData} or a single {@link LinkageData}.
 *
 * @see LinkageData
 */
public class ResourceRelationshipsDeserializer extends JsonDeserializer<ResourceRelationships> {

	private static final String DATA_FIELD_NAME = "data";
	
	private static final String LINKS_FIELD_NAME = "links";

	@Override
	public ResourceRelationships deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
		JsonNode node = jp.readValueAsTree();
		if (node == null) {
			return null;
		}
		ResourceRelationships resourceRelationships = new ResourceRelationships();

		Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
		while (fields.hasNext()) {
			Map.Entry<String, JsonNode> field = fields.next();
			Object value;
			if (field.getValue() == null) {
				throw new ParametersDeserializationException("Attribute field cannot be null for: " + field.getKey());
			}

			JsonNode linksNode = field.getValue().get(LINKS_FIELD_NAME);
			if(linksNode != null){
				resourceRelationships.setLinks(field.getKey(), linksNode);
			}
			JsonNode dataNode = field.getValue().get(DATA_FIELD_NAME);
			if (dataNode == null) {
				value = null;
			}
			else if (dataNode.isArray()) {
				Iterator<JsonNode> nodeIterator = dataNode.iterator();
				List<LinkageData> linkageDatas = new LinkedList<>();

				while (nodeIterator.hasNext()) {
					JsonNode linkageNode = nodeIterator.next();
					LinkageData newLinkageData = jp.getCodec().treeToValue(linkageNode, LinkageData.class);
					linkageDatas.add(newLinkageData);
				}
				value = linkageDatas;
			}
			else {
				value = jp.getCodec().treeToValue(dataNode, LinkageData.class);
			}

			resourceRelationships.setAdditionalProperty(field.getKey(), value);
		}

		return resourceRelationships;
	}
}
