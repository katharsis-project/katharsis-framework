package io.katharsis.vertx;

import io.katharsis.legacy.internal.RepositoryMethodParameterProvider;
import io.vertx.ext.web.RoutingContext;

/**
 * Factory to build parameter providers for each routing context.
 */
public interface ParameterProviderFactory {

    RepositoryMethodParameterProvider provider(RoutingContext ctx);

}
