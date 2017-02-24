package io.katharsis.core.internal.dispatcher.controller;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import io.katharsis.core.internal.repository.adapter.RelationshipRepositoryAdapter;
import io.katharsis.repository.request.HttpMethod;
import io.katharsis.repository.request.QueryAdapter;
import io.katharsis.resource.ResourceIdentifier;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.parser.TypeParser;

public class RelationshipsResourcePost extends RelationshipsResourceUpsert {

	public RelationshipsResourcePost(ResourceRegistry resourceRegistry, TypeParser typeParser) {
		super(resourceRegistry, typeParser);
	}

	@Override
	public HttpMethod method() {
		return HttpMethod.POST;
	}

	@Override
	public void processToManyRelationship(Object resource, Class<? extends Serializable> relationshipIdType, ResourceField resourceField, Iterable<ResourceIdentifier> dataBodies, QueryAdapter queryAdapter,
			RelationshipRepositoryAdapter relationshipRepositoryForClass) {
		List<Serializable> parsedIds = new LinkedList<>();

		for (ResourceIdentifier dataBody : dataBodies) {
			Serializable parsedId = typeParser.parse(dataBody.getId(), relationshipIdType);
			parsedIds.add(parsedId);
		}

		// noinspection unchecked
		relationshipRepositoryForClass.addRelations(resource, parsedIds, resourceField, queryAdapter);
	}

	@Override
	protected void processToOneRelationship(Object resource, Class<? extends Serializable> relationshipIdType, ResourceField resourceField, ResourceIdentifier dataBody, QueryAdapter queryAdapter,
			RelationshipRepositoryAdapter relationshipRepositoryForClass) {
		Serializable parsedId = null;
		if (dataBody != null) {
			parsedId = typeParser.parse(dataBody.getId(), relationshipIdType);
		}
		// noinspection unchecked
		relationshipRepositoryForClass.setRelation(resource, parsedId, resourceField, queryAdapter);
	}
}
