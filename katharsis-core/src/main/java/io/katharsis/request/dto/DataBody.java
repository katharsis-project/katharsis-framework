package io.katharsis.request.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.katharsis.jackson.deserializer.ResourceRelationshipsDeserializer;

public class DataBody {
    private String id;
    private String type;
	private JsonNode links;
	private JsonNode meta;

    @JsonDeserialize(using = ResourceRelationshipsDeserializer.class)
    private ResourceRelationships relationships;

    private JsonNode attributes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ResourceRelationships getRelationships() {
        return relationships;
    }

    public void setRelationships(ResourceRelationships relationships) {
        this.relationships = relationships;
    }

    public JsonNode getAttributes() {
        return attributes;
    }

    public void setAttributes(JsonNode attributes) {
        this.attributes = attributes;
    }
    
	public JsonNode getLinks() {
		return links;
	}

	public void setLinks(JsonNode links) {
		this.links = links;
	}
	
	public JsonNode getMeta() {
		return meta;
	}

	public void setMeta(JsonNode meta) {
		this.meta = meta;
	}
}
