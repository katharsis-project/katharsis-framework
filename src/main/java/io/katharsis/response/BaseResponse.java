package io.katharsis.response;

/**
 * Top-level JSON container's interface, used to generalize single and collection responses.
 *
 * @param <T> type of the response
 */
public interface BaseResponse<T> {

    int getStatus();
    T getData();
}
