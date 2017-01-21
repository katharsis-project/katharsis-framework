package io.katharsis.repository.request;

/**
 * Due to no RESTful dependencies, katharsis doesn't have any place to store a list of available HTTP methods, so
 * when referring to HTTP methods, this enum should be used.
 */
public enum HttpMethod {
    GET,
    POST,
    DELETE,
    PUT,
    PATCH
}
