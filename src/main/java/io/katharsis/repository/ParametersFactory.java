package io.katharsis.repository;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.exception.RepositoryMethodException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
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
     * @param method          repository method
     * @param queryParams     {@link QueryParams} object associated with the request
     * @param annotationType  method annotation
     * @return array of resolved parameters
     */
    public Object[] buildParameters(Object[] firstParameters, Method method, QueryParams queryParams,
                                    Class<? extends Annotation> annotationType) {
        Parameter[] parameters = method.getParameters();
        if (firstParameters.length > 0 && parameters.length < 1) {
            throw new RepositoryMethodException(
                String.format("Method with %s annotation should have at least one parameter.", annotationType));
        }
        Parameter[] parametersToResolve = Arrays.copyOfRange(parameters, firstParameters.length, parameters.length);
        Object[] additionalParameters = new Object[parametersToResolve.length];
        for (int i = 0; i < parametersToResolve.length; i++) {
            Parameter parameter = parametersToResolve[i];
            if (QueryParams.class.equals(parameter.getType())) {
                additionalParameters[i] = queryParams;
            } else {
                additionalParameters[i] = parameterProvider.provide(method, i + firstParameters.length);
            }
        }

        return concatenate(firstParameters, additionalParameters);
    }

    /**
     * Build a list of parameters that can be provided to a method.
     *
     * @param firstParameters parameters to be returned as the first elements in the return array
     * @param method          repository method
     * @param annotationType  method annotation
     * @return array of resolved parameters
     */
    public Object[] buildParameters(Object[] firstParameters, Method method,
                                    Class<? extends Annotation> annotationType) {
        Parameter[] parameters = method.getParameters();
        if (firstParameters.length > 0 && parameters.length < 1) {
            throw new RepositoryMethodException(
                String.format("Method with %s annotation should have at least one parameter.", annotationType));
        }
        Parameter[] parametersToResolve = Arrays.copyOfRange(parameters, firstParameters.length, parameters.length);
        Object[] additionalParameters = new Object[parametersToResolve.length];
        for (int i = 0; i < parametersToResolve.length; i++) {
            additionalParameters[i] = parameterProvider.provide(method, i + firstParameters.length);
        }

        return concatenate(firstParameters, additionalParameters);
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
