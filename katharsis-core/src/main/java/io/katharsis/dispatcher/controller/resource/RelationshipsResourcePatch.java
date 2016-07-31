package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.dto.DataBody;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.RelationshipRepositoryAdapter;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class RelationshipsResourcePatch extends RelationshipsResourceUpsert {

    public RelationshipsResourcePatch(ResourceRegistry resourceRegistry,
                                      TypeParser typeParser,
                                      QueryParamsBuilder paramsBuilder,
                                      ObjectMapper objectMapper) {
        super(resourceRegistry, typeParser, paramsBuilder, objectMapper);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.PATCH;
    }

    @Override
    public void processToManyRelationship(Object resource, RegistryEntry relationshipEntry,
                                          String elementName, Iterable<DataBody> dataBodies, QueryParams queryParams,
                                          RelationshipRepositoryAdapter relationshipRepositoryForClass) {
        List<Serializable> parsedIds = new LinkedList<>();
        for (DataBody dataBody : dataBodies) {
            Serializable parsedId = parseId(relationshipEntry, dataBody.getId());
            parsedIds.add(parsedId);
        }
        //noinspection unchecked
        relationshipRepositoryForClass.setRelations(resource, parsedIds, elementName, queryParams);
    }

    @Override
    protected void processToOneRelationship(Object resource, RegistryEntry relationshipEntry,
                                            String elementName, DataBody dataBody, QueryParams queryParams,
                                            RelationshipRepositoryAdapter relationshipRepositoryForClass) {
        Serializable parsedId = null;
        if (dataBody != null) {
            parsedId = parseId(relationshipEntry, dataBody.getId());
        }
        //noinspection unchecked
        relationshipRepositoryForClass.setRelation(resource, parsedId, elementName, queryParams);
    }
}
