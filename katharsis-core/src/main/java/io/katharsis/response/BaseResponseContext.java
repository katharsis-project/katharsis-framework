package io.katharsis.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.request.path.JsonPath;

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
