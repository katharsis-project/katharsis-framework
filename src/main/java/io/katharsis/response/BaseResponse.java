package io.katharsis.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Top-level JSON container's interface, used to generalize single and collection responses.
 *
 * @param <T> type of the response
 */
public interface BaseResponse<T> {

    @JsonIgnore
    int getHttpStatus();

    T getData();
}
