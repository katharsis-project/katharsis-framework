package io.katharsis.dispatcher.controller;

/**
 * Due to no RESTful dependencies, katharsis doesn't have any place to store a list of available HTTP methods, so
 * when referring to HTTP methods, this enum should be used.
 */
public enum HttpMethod {
    GET,
    POST,
    DELETE,
    PUT,
    PATCH;

    public static io.katharsis.dispatcher.controller.HttpMethod parse(String method) {
        if (method == null) {
            throw new IllegalStateException("Method should not be null.");
        }
        switch (method.toLowerCase()) {
            case "get":
                return GET;
            case "post":
                return POST;
            case "put":
                return PUT;
            case "patch":
                return PATCH;
            case "delete":
                return DELETE;
            default:
                throw new IllegalArgumentException("Method not supported " + method);
        }

    }
}
