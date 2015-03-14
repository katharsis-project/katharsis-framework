package io.katharsis.dispatcher.controller;

import io.katharsis.path.ResourcePath;
import io.katharsis.response.BaseResponse;

public class ResourceGet implements BaseController {
    @Override
    public boolean isAcceptable(ResourcePath resourcePath, String requestType) {
        return resourcePath.getIds() != null && "GET".equals(requestType);
    }

    @Override
    public BaseResponse<?> accept(ResourcePath resourcePath) {
        return null;
    }
}
