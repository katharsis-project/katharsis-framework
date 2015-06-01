package io.katharsis.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.katharsis.request.dto.Linkage;
import io.katharsis.request.dto.ResourceRelationships;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Deserialize ResourceLinks field which can contain either a list of {@link Linkage} or a single {@link Linkage}.
 *
 * @see Linkage
 */
public class ResourceRelationshipsDeserializer extends JsonDeserializer<ResourceRelationships> {
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
                value = null;
            } else if (field.getValue().isArray()) {
                Iterator<JsonNode> nodeIterator = field.getValue().iterator();
                List<Linkage> linkages = new LinkedList<>();

                while (nodeIterator.hasNext()) {
                    Linkage newLinkage = jp.getCodec().treeToValue(nodeIterator.next(), Linkage.class);
                    linkages.add(newLinkage);
                }
                value = linkages;
            } else {
                value = jp.getCodec().treeToValue(field.getValue(), Linkage.class);
            }
            resourceRelationships.setAdditionalProperty(field.getKey(), value);
        }

        return resourceRelationships;
    }
}
