package io.katharsis.request;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.errorhandling.GenericKatharsisException;
import io.katharsis.repository.RepositoryParameterProvider;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.utils.java.Optional;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static io.katharsis.request.Request.JsonApiRequestType.COLLECTION;
import static io.katharsis.request.Request.JsonApiRequestType.COLLECTION_IDS;
import static io.katharsis.request.Request.JsonApiRequestType.FIELD;
import static io.katharsis.request.Request.JsonApiRequestType.RELATIONSHIP;
import static io.katharsis.request.Request.JsonApiRequestType.SINGLE_RESOURCE;

/**
 * Katharsis Domain object that holds for the request data.
 * <p/>
 * The body InputStream is not closed by Katharsis.
 */
public class Request {

    private final HttpMethod method;
    private final JsonApiPath path;
    private final Optional<InputStream> body;

    private final RepositoryParameterProvider parameterProvider;

    public Request(JsonApiPath path, String method, InputStream body, RepositoryParameterProvider parameterProvider) {
        this.path = path;
        this.method = HttpMethod.parse(method);
        this.body = Optional.ofNullable(body);
        this.parameterProvider = parameterProvider;
    }

    private static URL parseURL(String uri) throws GenericKatharsisException {
        try {

            return URI.create(uri).toURL();
        } catch (MalformedURLException e) {
            throw new GenericKatharsisException("Invalid URL " + e.getMessage());
        }
    }

    public JsonApiPath getPath() {
        return path;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public Optional<InputStream> getBody() {
        return body;
    }

    public Optional<String> getQuery() {
        return path.getQuery();
    }


    public RepositoryParameterProvider getParameterProvider() {
        return parameterProvider;
    }

    public JsonApiRequestType requestType() {
        if (path.getField().isPresent()) {
            return FIELD;
        }

        if (path.getRelationship().isPresent()) {
            return RELATIONSHIP;
        }

        if (path.getIds().isPresent()) {
            if (path.getIds().get().size() > 1) {
                return COLLECTION_IDS;
            }
            return SINGLE_RESOURCE;
        } else {
            return COLLECTION;
        }
    }

    @Override
    public String toString() {
        return "path=" + path + ", method=" + method;
    }

    /**
     * Determine the 'state' of the request by looking at the JSON API Path.
     */
    public enum JsonApiRequestType {
        COLLECTION,
        COLLECTION_IDS,
        SINGLE_RESOURCE,
        FIELD,
        RELATIONSHIP
    }
}
