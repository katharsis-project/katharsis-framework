package io.katharsis.dispatcher.controller;

import io.katharsis.path.ResourcePath;
import io.katharsis.response.BaseResponse;

public interface BaseController {
    boolean isAcceptable(ResourcePath resourcePath, String requestType);

    BaseResponse<?> accept(ResourcePath resourcePath);
}
