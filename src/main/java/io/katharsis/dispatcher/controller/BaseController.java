package io.katharsis.dispatcher.controller;

import io.katharsis.path.JsonPath;
import io.katharsis.response.BaseResponse;

public interface BaseController {
    boolean isAcceptable(JsonPath jsonPath, String requestType);

    BaseResponse<?> handle(JsonPath jsonPath);
}
