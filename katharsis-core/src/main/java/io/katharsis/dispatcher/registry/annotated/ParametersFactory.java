package io.katharsis.dispatcher.registry.annotated;

import io.katharsis.query.QueryParams;
import io.katharsis.repository.RepositoryParameterProvider;
import io.katharsis.repository.exception.RepositoryMethodException;

import java.lang.reflect.Method;

public class ParametersFactory {

    /**
     * Source: https://stackoverflow.com/a/80503
     */
    private static Object[] concatenate(Object[] a, Object[] b) {

        int aLen = a.length;
        int bLen = b.length;

        Object[] newArray = new Object[aLen + bLen];
        System.arraycopy(a, 0, newArray, 0, aLen);
        System.arraycopy(b, 0, newArray, aLen, bLen);

        return newArray;
    }

    /**
     * Build a list of parameters that can be provided to a method.
     *
     * @param firstParameters parameters to be returned as the firsts element in the return array
     * @param method          repository method
     * @param queryParams     {@link QueryParams} object associated with the request
     * @return array of resolved parameters
     */
    public Object[] buildParameters(RepositoryParameterProvider parameterProvider, Object[] firstParameters, Method method, QueryParams queryParams) {
        int parametersLength = method.getParameterTypes().length;
        if (firstParameters.length > 0 && parametersLength < 1) {
            throw new RepositoryMethodException(
                    String.format("Method with %s annotation should have at least one parameter.", method));
        }
        int parametersToResolve = parametersLength - firstParameters.length;
        Object[] additionalParameters = new Object[parametersToResolve];
        for (int i = firstParameters.length; i < parametersLength; i++) {
            Class<?> parameterType = method.getParameterTypes()[i];
            if (QueryParams.class.equals(parameterType)) {
                additionalParameters[i - firstParameters.length] = queryParams;
            } else {
                additionalParameters[i - firstParameters.length] = parameterProvider.provide(method, i);
            }
        }

        return concatenate(firstParameters, additionalParameters);
    }

    /**
     * Build a list of parameters that can be provided to a method.
     *
     * @param firstParameters parameters to be returned as the first elements in the return array
     * @param method          repository method
     * @return array of resolved parameters
     */
    public Object[] buildParameters(RepositoryParameterProvider parameterProvider, Object[] firstParameters, Method method) {
        int parametersLength = method.getParameterTypes().length;
        if (firstParameters.length > 0 && parametersLength < 1) {
            throw new RepositoryMethodException(
                    String.format("Method with %s annotation should have at least one parameter.", method));
        }
        int parametersToResolve = parametersLength - firstParameters.length;
        Object[] additionalParameters = new Object[parametersToResolve];
        for (int i = firstParameters.length; i < parametersLength; i++) {
            additionalParameters[i - firstParameters.length] = parameterProvider.provide(method, i);
        }

        return concatenate(firstParameters, additionalParameters);
    }
}
