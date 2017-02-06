package io.katharsis.vertx;

import java.lang.reflect.Method;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.legacy.internal.RepositoryMethodParameterProvider;
import io.vertx.ext.web.RoutingContext;

/**
 * The {@link RepositoryMethodParameterProvider RepositoryMethodParameterProvider}
 * allows you to inject object into your repository methods.
 */
public class DefaultParameterProvider implements RepositoryMethodParameterProvider {

  private final ObjectMapper mapper;

  private final RoutingContext ctx;

  public DefaultParameterProvider(ObjectMapper mapper, RoutingContext ctx) {
    this.mapper = mapper;
    this.ctx = ctx;
  }

  @Override
  public <T> T provide(Method method, int parameterIndex) {
    Class<?> parameter = method.getParameterTypes()[parameterIndex];
    Object returnValue = null;
    if (RoutingContext.class.isAssignableFrom(parameter)) {
      returnValue = ctx;
    }
    else if (ObjectMapper.class.isAssignableFrom(parameter)) {
      returnValue = mapper;
    }
    return (T) returnValue;
  }

  public ObjectMapper getMapper() {
    return mapper;
  }

  public RoutingContext getCtx() {
    return ctx;
  }
}
