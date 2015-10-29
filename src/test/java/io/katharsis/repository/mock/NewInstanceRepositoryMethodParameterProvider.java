package io.katharsis.repository.mock;

import io.katharsis.repository.RepositoryMethodParameterProvider;

import java.lang.reflect.Parameter;

public class NewInstanceRepositoryMethodParameterProvider implements RepositoryMethodParameterProvider {

    @Override
    public <T> T provide(Parameter parameter) {
        try {
            return (T) parameter.getType().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}