package io.katharsis.vertx;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.vertx.ext.web.RoutingContext;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;

/**
 * The {@link RepositoryMethodParameterProvider RepositoryMethodParameterProvider}
 * allows you to inject object into your repository methods.
 */
@Data
@RequiredArgsConstructor
public class DefaultParameterProvider implements RepositoryMethodParameterProvider {

    private final ObjectMapper mapper;
    private final RoutingContext ctx;

    @Override
    public <T> T provide(Method method, int parameterIndex) {
        Class<?> parameter = method.getParameterTypes()[parameterIndex];
        Object returnValue = null;
        if (RoutingContext.class.isAssignableFrom(parameter)) {
            returnValue = ctx;
        } else if (ObjectMapper.class.isAssignableFrom(parameter)) {
            returnValue = mapper;
        }
        return (T) returnValue;
    }
}
