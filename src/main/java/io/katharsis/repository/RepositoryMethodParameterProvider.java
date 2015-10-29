package io.katharsis.repository;

import java.lang.reflect.Parameter;

/**
 * Provides additional parameters for a repository method.
 */
public interface RepositoryMethodParameterProvider {

    <T> T provide(Parameter parameter);
}
