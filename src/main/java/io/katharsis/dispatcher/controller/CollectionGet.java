package io.katharsis.dispatcher.controller;

import io.katharsis.path.ResourcePath;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;

public class CollectionGet implements BaseController {

    private ResourceRegistry resourceRegistry;

    public CollectionGet(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public boolean isAcceptable(ResourcePath resourcePath, String requestType) {
        return resourcePath.getIds() == null && "GET".equals(requestType);
    }

    @Override
    public BaseResponse<?> accept(ResourcePath resourcePath) {

        return null;
    }
}
