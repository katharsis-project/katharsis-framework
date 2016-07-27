package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.request.dto.DataBody;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.RelationshipRepositoryAdapter;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

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
                                          String elementName, Iterable<DataBody> dataBodies, QueryParams queryParams,
                                          RelationshipRepositoryAdapter relationshipRepositoryForClass) {
        List<Serializable> parsedIds = new LinkedList<>();

        for (DataBody dataBody : dataBodies) {
            Serializable parsedId = typeParser.parse(dataBody.getId(), relationshipIdType);
            parsedIds.add(parsedId);
        }

        //noinspection unchecked
        relationshipRepositoryForClass.addRelations(resource, parsedIds, elementName, queryParams);
    }

    @Override
    protected void processToOneRelationship(Object resource, Class<? extends Serializable> relationshipIdType,
                                            String elementName, DataBody dataBody, QueryParams queryParams,
                                            RelationshipRepositoryAdapter relationshipRepositoryForClass) {
        Serializable parsedId = null;
        if (dataBody != null) {
            parsedId = typeParser.parse(dataBody.getId(), relationshipIdType);
        }
        //noinspection unchecked
        relationshipRepositoryForClass.setRelation(resource, parsedId, elementName, queryParams);
    }
}
