package io.katharsis.vertx;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.legacy.internal.RepositoryMethodParameterProvider;
import io.vertx.ext.web.RoutingContext;

/**
 * Parameter Factory - injects objects from the request context in the repository method.
 */
public class DefaultParameterProviderFactory implements ParameterProviderFactory {

  private ObjectMapper mapper;

  @Override
  public RepositoryMethodParameterProvider provider(RoutingContext ctx) {
    return new DefaultParameterProvider(mapper, ctx);
  }

  public ObjectMapper getMapper() {
    return mapper;
  }

}
