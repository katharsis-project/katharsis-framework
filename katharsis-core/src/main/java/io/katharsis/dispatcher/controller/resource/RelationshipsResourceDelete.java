package io.katharsis.dispatcher.controller.resource;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.resource.ResourceIdentifier;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.repository.adapter.RelationshipRepositoryAdapter;
import io.katharsis.utils.parser.TypeParser;

public class RelationshipsResourceDelete extends RelationshipsResourceUpsert {

    public RelationshipsResourceDelete(ResourceRegistry resourceRegistry, TypeParser typeParser) {
        super(resourceRegistry, typeParser);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.DELETE;
    }

    @Override
    public void processToManyRelationship(Object resource, Class<? extends Serializable> relationshipIdType,
                                          String elementName, Iterable<ResourceIdentifier> dataBodies, QueryAdapter queryAdapter,
                                          RelationshipRepositoryAdapter relationshipRepositoryForClass) {
        List<Serializable> parsedIds = new LinkedList<>();
        for (ResourceIdentifier dataBody : dataBodies) {
            Serializable parsedId = typeParser.parse(dataBody.getId(), relationshipIdType);
            parsedIds.add(parsedId);
        }
        //noinspection unchecked
        relationshipRepositoryForClass.removeRelations(resource, parsedIds, elementName, queryAdapter);
    }

    @Override
    protected void processToOneRelationship(Object resource, Class<? extends Serializable> relationshipIdType,
                                            String elementName, ResourceIdentifier dataBody, QueryAdapter queryAdapter,
                                            RelationshipRepositoryAdapter relationshipRepositoryForClass) {
        //noinspection unchecked
        relationshipRepositoryForClass.setRelation(resource, null, elementName, queryAdapter);
    }

}
