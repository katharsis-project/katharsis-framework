package io.katharsis.repository.mock;

import java.lang.reflect.Method;

import io.katharsis.legacy.internal.RepositoryMethodParameterProvider;

public class NewInstanceRepositoryMethodParameterProvider implements RepositoryMethodParameterProvider {


    @Override
    public <T> T provide(Method method, int parameterIndex) {
        Class<?> aClass = method.getParameterTypes()[parameterIndex];

        try {
            return (T) aClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}