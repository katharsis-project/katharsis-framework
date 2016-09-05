package io.katharsis.dispatcher.controller.relationship;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.RelationshipsPath;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.*;
import io.katharsis.utils.parser.TypeParser;

public class RelationshipsResourceHead extends RelationshipsResourceGet {

    public RelationshipsResourceHead(ResourceRegistry resourceRegistry, TypeParser typeParser, IncludeLookupSetter fieldSetter) {
        super(resourceRegistry, typeParser, fieldSetter);
    }

    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection()
                && jsonPath instanceof RelationshipsPath
                && HttpMethod.HEAD.name().equals(requestType);
    }

    @Override
    public BaseResponseContext handle(JsonPath jsonPath, QueryParams queryParams,
                                      RepositoryMethodParameterProvider parameterProvider, RequestBody requestBody) {
        buildResponse(jsonPath, queryParams, parameterProvider);

        return null;
    }
}
