package io.katharsis.request.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.katharsis.jackson.deserializer.ResourceRelationshipsDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataBody {

    private String id;
    private String type;

    @JsonDeserialize(using = ResourceRelationshipsDeserializer.class)
    private ResourceRelationships relationships;
    private JsonNode attributes;

}
