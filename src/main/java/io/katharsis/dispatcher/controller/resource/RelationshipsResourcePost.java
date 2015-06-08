package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.RelationshipsPath;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
import io.katharsis.utils.parser.TypeParser;

public class RelationshipsResourcePost implements BaseController {

    private ResourceRegistry resourceRegistry;
    private TypeParser typeParser;

    public RelationshipsResourcePost(ResourceRegistry resourceRegistry, TypeParser typeParser) {
        this.resourceRegistry = resourceRegistry;
        this.typeParser = typeParser;
    }

    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection()
                && jsonPath instanceof RelationshipsPath
                && HttpMethod.POST.name().equals(requestType);
    }

    @Override
    public BaseResponse<?> handle(JsonPath jsonPath, RequestParams requestParams, RequestBody requestBody) throws Exception {
        return null;
    }

}
