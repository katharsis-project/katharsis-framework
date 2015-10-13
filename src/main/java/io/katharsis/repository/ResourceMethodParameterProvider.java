package io.katharsis.repository;

import java.lang.reflect.Parameter;

/**
 * Provides additional parameters for a method.
 */
public interface ResourceMethodParameterProvider {

    <T> T provide(Parameter parameter);
}
