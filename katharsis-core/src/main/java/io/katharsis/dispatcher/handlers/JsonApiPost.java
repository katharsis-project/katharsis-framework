package io.katharsis.dispatcher.handlers;

import io.katharsis.dispatcher.ResponseContext;
import io.katharsis.dispatcher.registry.api.RepositoryRegistry;
import io.katharsis.request.Request;
import lombok.Data;

@Data
public class JsonApiPost implements JsonApiHandler {

    private final RepositoryRegistry registry;

    @Override
    public ResponseContext handle(Request request) {
        return null;
    }
}
