package io.katharsis.dispatcher.handlers;

import io.katharsis.dispatcher.ResponseContext;
import io.katharsis.request.Request;

/**
 * Handles a JSON-API request.
 */
public interface JsonApiHandler {

    ResponseContext handle(Request request);
}
