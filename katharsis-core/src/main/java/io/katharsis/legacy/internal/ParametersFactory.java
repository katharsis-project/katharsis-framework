package io.katharsis.legacy.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import io.katharsis.core.internal.query.QuerySpecAdapter;
import io.katharsis.errorhandling.exception.RepositoryMethodException;
import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.request.QueryAdapter;

public class ParametersFactory {

    private final RepositoryMethodParameterProvider parameterProvider;
	private ModuleRegistry moduleRegistry;

    public ParametersFactory(ModuleRegistry moduleRegistry, RepositoryMethodParameterProvider parameterProvider) {
        this.parameterProvider = parameterProvider;
		this.moduleRegistry = moduleRegistry;
    }

    /**
     * Build a list of parameters that can be provided to a method.
     *
     * @param firstParameters parameters to be returned as the firsts element in the return array
     * @param method          repository method
     * @param annotationType  method annotation
     * @param queryAdapter Ask remmo
     * @return array of resolved parameters
     */
    public Object[] buildParameters(Object[] firstParameters, Method method, QueryAdapter queryAdapter,
                                    Class<? extends Annotation> annotationType) {
        int parametersLength = method.getParameterTypes().length;
        if (firstParameters.length > 0 && parametersLength < 1) {
            throw new RepositoryMethodException(
                String.format("Method with %s annotation should have at least one parameter.", annotationType));
        }
        int parametersToResolve = parametersLength - firstParameters.length;
        Object[] additionalParameters = new Object[parametersToResolve];
        for (int i = firstParameters.length; i < parametersLength; i++) {
            Class<?> parameterType = method.getParameterTypes()[i];
            if (QueryParams.class.equals(parameterType)) {
                additionalParameters[i - firstParameters.length] = toQueryParams(queryAdapter);
            } else if(QuerySpec.class.equals(parameterType)) {
                additionalParameters[i - firstParameters.length] = toQuerySpec(queryAdapter);
            } else {
                additionalParameters[i - firstParameters.length] = parameterProvider.provide(method, i);
            }
        }

        return concatenate(firstParameters, additionalParameters);
    }
    
    protected QuerySpec toQuerySpec(QueryAdapter queryAdapter) {
    	if (queryAdapter == null)
			return null;
		if (queryAdapter instanceof QuerySpecAdapter) {
			return ((QuerySpecAdapter) queryAdapter).getQuerySpec();
		}
		QueryParams queryParams = toQueryParams(queryAdapter);
		DefaultQuerySpecConverter converter = new DefaultQuerySpecConverter(moduleRegistry);
		return converter.fromParams(queryAdapter.getResourceInformation().getResourceClass(), queryParams);
	}

	protected QueryParams toQueryParams(QueryAdapter queryAdapter) {
		if (queryAdapter == null)
			return null;
		if(!(queryAdapter instanceof QueryParamsAdapter))
			throw new IllegalStateException("consider rewriting your repository to use QuerySpec instead of QueryParams, or disable QuerySpec parsing");
		return ((QueryParamsAdapter) queryAdapter).getQueryParams();
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
        int parametersLength = method.getParameterTypes().length;
        if (firstParameters.length > 0 && parametersLength < 1) {
            throw new RepositoryMethodException(
                String.format("Method with %s annotation should have at least one parameter.", annotationType));
        }
        int parametersToResolve = parametersLength - firstParameters.length;
        Object[] additionalParameters = new Object[parametersToResolve];
        for (int i = firstParameters.length; i < parametersLength; i++) {
            additionalParameters[i - firstParameters.length] = parameterProvider.provide(method, i);
        }

        return concatenate(firstParameters, additionalParameters);
    }

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
}
