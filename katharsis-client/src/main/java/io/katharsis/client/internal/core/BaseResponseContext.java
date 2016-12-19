package io.katharsis.client.internal.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.request.path.JsonPath;
import io.katharsis.response.JsonApiResponse;

/**
 * Top-level JSON container's interface, used to generalize single and collection responses.
 */
public interface BaseResponseContext {

    @JsonIgnore
    int getHttpStatus();

    JsonApiResponse getResponse();

    JsonPath getJsonPath();

    QueryAdapter getQueryAdapter();
}
