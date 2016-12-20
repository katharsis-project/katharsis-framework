package io.katharsis.resource.internal;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.resource.Document;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.JsonApiResponse;

public class DocumentMapper {

	private ResourceRegistry resourceRegistry;
	private ObjectMapper objectMapper;

	public DocumentMapper(ResourceRegistry resourceRegistry, ObjectMapper objectMapper) {
		this.resourceRegistry = resourceRegistry;
		this.objectMapper = objectMapper;
	}

	public Document toDocument(JsonApiResponse response) {
		
		
		
		// TODO Auto-generated method stub
		return null;
	}

}
