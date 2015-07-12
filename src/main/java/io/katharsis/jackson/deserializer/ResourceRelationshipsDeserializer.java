package io.katharsis.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.katharsis.jackson.exception.ParametersDeserializationException;
import io.katharsis.request.dto.LinkageData;
import io.katharsis.request.dto.ResourceRelationships;

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
            } else if (field.getValue().get(DATA_FIELD_NAME) == null) {
                value = null;
            } else if (field.getValue().get(DATA_FIELD_NAME).isArray()) {
                JsonNode fieldData = field.getValue().get(DATA_FIELD_NAME);
                Iterator<JsonNode> nodeIterator = fieldData.iterator();
                List<LinkageData> linkageDatas = new LinkedList<>();

                while (nodeIterator.hasNext()) {
                    LinkageData newLinkageData = jp.getCodec().treeToValue(nodeIterator.next(), LinkageData.class);
                    linkageDatas.add(newLinkageData);
                }
                value = linkageDatas;
            } else {
                value = jp.getCodec().treeToValue(field.getValue().get(DATA_FIELD_NAME), LinkageData.class);
            }
            resourceRelationships.setAdditionalProperty(field.getKey(), value);
        }

        return resourceRelationships;
    }
}
