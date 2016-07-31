package io.katharsis.repository.mock;

import io.katharsis.repository.RepositoryParameterProvider;

import java.lang.reflect.Method;

public class NewInstanceRepositoryParameterProvider implements RepositoryParameterProvider {

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