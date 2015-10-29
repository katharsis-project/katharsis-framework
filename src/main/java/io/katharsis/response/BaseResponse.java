package io.katharsis.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.request.path.JsonPath;

/**
 * Top-level JSON container's interface, used to generalize single and collection responses.
 *
 * @param <T> type of the response
 */
public interface BaseResponse<T> {

    @JsonIgnore
    int getHttpStatus();

    T getData();

    JsonPath getJsonPath();

    QueryParams getQueryParams();

    MetaInformation getMetaInformation();

    LinksInformation getLinksInformation();
}
