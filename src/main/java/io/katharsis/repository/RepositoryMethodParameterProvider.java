package io.katharsis.repository;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Provides additional parameters for a repository method.
 */
public interface RepositoryMethodParameterProvider {

    <T> T provide(Method method, int parameterIndex);

    default Parameter getParameter(Method method, int parameterIndex) {
        return method.getParameters()[parameterIndex];
    }
}
