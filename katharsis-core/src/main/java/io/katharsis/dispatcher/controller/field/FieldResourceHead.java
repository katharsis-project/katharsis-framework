package io.katharsis.dispatcher.controller.field;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.dispatcher.controller.resource.ResourceIncludeField;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.FieldPath;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathIds;
import io.katharsis.resource.exception.ResourceFieldNotFoundException;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.RelationshipRepositoryAdapter;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.CollectionResponseContext;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.ResourceResponseContext;
import io.katharsis.utils.Generics;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;

public class FieldResourceHead extends FieldResourceGet {

    public FieldResourceHead(ResourceRegistry resourceRegistry, TypeParser typeParser, IncludeLookupSetter fieldSetter) {
        super(resourceRegistry,typeParser,fieldSetter);
    }

    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection()
                && FieldPath.class.equals(jsonPath.getClass())
                && HttpMethod.HEAD.name().equals(requestType);
    }

    @Override
    public BaseResponseContext handle(JsonPath jsonPath, QueryParams queryParams, RepositoryMethodParameterProvider
        parameterProvider, RequestBody requestBody) {

        buildResponse(jsonPath, queryParams, parameterProvider);

        return null;
    }
}
