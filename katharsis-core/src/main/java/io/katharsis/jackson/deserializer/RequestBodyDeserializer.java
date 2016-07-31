package io.katharsis.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.RequestBody;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class RequestBodyDeserializer extends JsonDeserializer<RequestBody> {

    @Override
    public RequestBody deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jp.readValueAsTree();
        if (node == null) {
            return null;
        }
        RequestBody requestBody = new RequestBody();
        JsonNode data = node.get("data");
        Object value;
        if (data != null) {
            if (data.isArray()) {
                Iterator<JsonNode> nodeIterator = data.iterator();
                List<DataBody> dataBodies = new LinkedList<>();

                while (nodeIterator.hasNext()) {
                    DataBody newLinkage = jp.getCodec().treeToValue(nodeIterator.next(), DataBody.class);
                    dataBodies.add(newLinkage);
                }
                value = dataBodies;
            } else if (data.isObject()) {
                value = jp.getCodec().treeToValue(data, DataBody.class);
            } else if (data.isNull()) {
                value = null;
            } else {
                throw new RuntimeException("data field has wrong type: " + data.toString());
            }
            requestBody.setData(value);
        }

        return requestBody;
    }
}
