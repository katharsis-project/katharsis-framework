package io.katharsis.vertx;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * Respond with json api media type.
 */
public class JsonApiMediaTypeHandler implements Handler<RoutingContext> {
    /**
     * A {@code String} constant representing {@value #APPLICATION_JSON_API} media type.
     */
    public final static String APPLICATION_JSON_API = "application/vnd.api+json";

    public static final JsonApiMediaTypeHandler INSTANCE = new JsonApiMediaTypeHandler();

    @Override
    public void handle(RoutingContext ctx) {
        ctx.response().putHeader("Content-type", APPLICATION_JSON_API);
        ctx.next();
    }
}
