package io.katharsis.vertx;

import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.vertx.ext.web.RoutingContext;

/**
 * Factory to build parameter providers for each routing context.
 */
public interface ParameterProviderFactory {

    RepositoryMethodParameterProvider provider(RoutingContext ctx);

}
