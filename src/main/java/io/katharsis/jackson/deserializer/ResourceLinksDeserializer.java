package io.katharsis.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.katharsis.request.dto.Linkage;
import io.katharsis.request.dto.ResourceLinks;

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
public class ResourceLinksDeserializer extends JsonDeserializer<ResourceLinks> {
    @Override
    public ResourceLinks deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.readValueAsTree();
        if (node == null) {
            return null;
        }
        ResourceLinks resourceLinks = new ResourceLinks();

        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            Object value;
            if (field.getValue() == null) {
                value = null;
            } else if (field.getValue().isArray()) {
                Iterator<Linkage> linkageIterator = jp.readValuesAs(Linkage.class);
                List<Linkage> linkages = new LinkedList<>();
                while (linkageIterator.hasNext()) {
                    linkages.add(linkageIterator.next());
                }
                value = linkages;
            } else {
                value = jp.readValueAs(Linkage.class);
            }
            resourceLinks.setAdditionalProperty(field.getKey(), value);
        }

        return resourceLinks;
    }
}
