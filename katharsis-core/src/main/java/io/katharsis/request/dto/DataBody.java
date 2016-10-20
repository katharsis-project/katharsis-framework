package io.katharsis.request.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.katharsis.jackson.deserializer.ResourceRelationshipsDeserializer;
import lombok.Getter;

@lombok.Getter
@lombok.Setter
@lombok.EqualsAndHashCode
@lombok.Data
public class DataBody {
    private String id;
    private String type;

    @Getter(onMethod = @__(@JsonDeserialize(using = ResourceRelationshipsDeserializer.class)))
    private ResourceRelationships relationships;

    private JsonNode attributes;

}
