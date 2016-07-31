package io.katharsis.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.request.path.JsonApiPath;

/**
 * Top-level JSON container's interface, used to generalize single and collection responses.
 */
public interface BaseResponseContext {

    @JsonIgnore
    int getHttpStatus();

    @JsonIgnore
    void setHttpStatus(int newStatus);

    JsonApiResponse getResponse();

    JsonApiPath getPath();

    QueryParams getQueryParams();
}
