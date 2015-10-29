package io.katharsis.repository;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.exception.RepositoryMethodException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Arrays;

public class ParametersFactory {

    private final RepositoryMethodParameterProvider parameterProvider;

    public ParametersFactory(RepositoryMethodParameterProvider parameterProvider) {
        this.parameterProvider = parameterProvider;
    }

    /**
     * Build a list of parameters that can be provided to a method.
     *
     * @param firstParameters parameters to be returned as the firsts element in the return array
     * @param parameters     parameters to be resolved
     * @param queryParams  {@link QueryParams} object associated with the request
     * @param annotationType method annotation
     * @return array of resolved parameters
     */
    public Object[] buildParameters(Object[] firstParameters, Parameter[] parameters, QueryParams queryParams,
                                    Class<? extends Annotation> annotationType) {
        if (parameters.length < 1) {
            throw new RepositoryMethodException(
                String.format("Method with %s annotation should have at least one parameter.", annotationType));
        }
        Parameter[] parametersToResolve = Arrays.copyOfRange(parameters, firstParameters.length, parameters.length);
        Object[] additionalParameters = buildParameters(parametersToResolve, queryParams);

        return concatenate(firstParameters, additionalParameters);
    }

    /**
     * Build a list of parameters that can be provided to a method.
     *
     * @param firstParameters parameters to be returned as the first elements in the return array
     * @param parameters     parameters to be resolved
     * @param annotationType method annotation
     * @return array of resolved parameters
     */
    public Object[] buildParameters(Object[] firstParameters, Parameter[] parameters,
                                    Class<? extends Annotation> annotationType) {
        if (parameters.length < 1) {
            throw new RepositoryMethodException(
                String.format("Method with %s annotation should have at least one parameter.", annotationType));
        }
        Parameter[] parametersToResolve = Arrays.copyOfRange(parameters, firstParameters.length, parameters.length);
        Object[] additionalParameters = buildParameters(parametersToResolve);

        return concatenate(firstParameters, additionalParameters);
    }

    /**
     * Build a list of parameters that can be provided to a method.
     *
     * @param parameters    parameters to be resolved
     * @param queryParams {@link QueryParams} object associated with the request
     * @return array of resolved parameters
     */
    public Object[] buildParameters(Parameter[] parameters, QueryParams queryParams) {
        Object[] parameterValues = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (QueryParams.class.equals(parameter.getType())) {
                parameterValues[i] = queryParams;
            } else {
                parameterValues[i] = parameterProvider.provide(parameter);
            }
        }
        return parameterValues;
    }

    /**
     * Build a list of parameters that can be provided to a method.
     *
     * @param parameters parameters to be resolved
     * @return array of resolved parameters
     */
    private Object[] buildParameters(Parameter[] parameters) {
        Object[] parameterValues = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterValues[i] = parameterProvider.provide(parameters[i]);
        }
        return parameterValues;
    }

    /**
     * Source: https://stackoverflow.com/a/80503
     */
    private Object[] concatenate(Object[] a, Object[] b) {

        int aLen = a.length;
        int bLen = b.length;

        Object[] newArray = new Object[aLen + bLen];
        System.arraycopy(a, 0, newArray, 0, aLen);
        System.arraycopy(b, 0, newArray, aLen, bLen);

        return newArray;
    }
}
