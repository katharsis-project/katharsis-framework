package io.katharsis.dispatcher.controller.resource;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.resource.ResourceId;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.repository.adapter.RelationshipRepositoryAdapter;
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
    public void processToManyRelationship(Object resource, Class<? extends Serializable> relationshipIdType,
                                          String elementName, Iterable<ResourceId> dataBodies, QueryAdapter queryAdapter,
                                          RelationshipRepositoryAdapter relationshipRepositoryForClass) {
        List<Serializable> parsedIds = new LinkedList<>();

        for (ResourceId dataBody : dataBodies) {
            Serializable parsedId = typeParser.parse(dataBody.getId(), relationshipIdType);
            parsedIds.add(parsedId);
        }

        //noinspection unchecked
        relationshipRepositoryForClass.addRelations(resource, parsedIds, elementName, queryAdapter);
    }

    @Override
    protected void processToOneRelationship(Object resource, Class<? extends Serializable> relationshipIdType,
                                            String elementName, ResourceId dataBody, QueryAdapter queryAdapter,
                                            RelationshipRepositoryAdapter relationshipRepositoryForClass) {
        Serializable parsedId = null;
        if (dataBody != null) {
            parsedId = typeParser.parse(dataBody.getId(), relationshipIdType);
        }
        //noinspection unchecked
        relationshipRepositoryForClass.setRelation(resource, parsedId, elementName, queryAdapter);
    }
}
