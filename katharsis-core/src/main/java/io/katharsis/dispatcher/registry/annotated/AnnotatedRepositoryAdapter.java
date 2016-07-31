package io.katharsis.dispatcher.registry.annotated;

import io.katharsis.dispatcher.registry.api.LinksRepository;
import io.katharsis.dispatcher.registry.api.MetaRepository;
import io.katharsis.domain.api.LinksInformation;
import io.katharsis.domain.api.MetaInformation;
import io.katharsis.query.QueryParams;
import io.katharsis.repository.RepositoryParameterProvider;
import io.katharsis.repository.annotations.JsonApiLinks;
import io.katharsis.repository.annotations.JsonApiMeta;
import io.katharsis.repository.exception.RepositoryAnnotationNotFoundException;
import io.katharsis.utils.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class AnnotatedRepositoryAdapter<T> implements LinksRepository<T>, MetaRepository<T> {

    final Object implementationObject;
    final Class<?> implementationClass;
    final ParametersFactory parametersFactory;

    private Method linksMethod;
    private Method metaMethod;

    public AnnotatedRepositoryAdapter(Object implementationObject, ParametersFactory parametersFactory) {
        this.implementationObject = implementationObject;
        this.implementationClass = implementationObject.getClass();
        this.parametersFactory = parametersFactory;

        linksMethod = ClassUtils.findMethodWith(implementationClass, JsonApiLinks.class);
        metaMethod = ClassUtils.findMethodWith(implementationClass, JsonApiMeta.class);
    }

    @Override
    public LinksInformation getLinksInformation(RepositoryParameterProvider parameterProvider, Iterable<T> resources, QueryParams queryParams) {
        checkIfNotNull(linksMethod, JsonApiLinks.class);

        Object[] methodParameters = parametersFactory
                .buildParameters(parameterProvider, new Object[]{resources}, linksMethod, queryParams);

        return invoke(linksMethod, methodParameters);
    }

    @Override
    public MetaInformation getMetaInformation(RepositoryParameterProvider parameterProvider, Iterable<T> resources, QueryParams queryParams) {
        checkIfNotNull(metaMethod, JsonApiMeta.class);

        Object[] methodParameters = parametersFactory
                .buildParameters(parameterProvider, new Object[]{resources}, metaMethod, queryParams);

        return invoke(metaMethod, methodParameters);
    }

    protected void checkIfNotNull(Method foundMethod, Class<? extends Annotation> annotationClass) {
        if (foundMethod == null) {
            throw new RepositoryAnnotationNotFoundException(
                    String.format("Annotation %s for class %s not found", annotationClass, implementationObject.getClass()));
        }
    }

    protected <TYPE> TYPE invokeOperation(RepositoryParameterProvider parameterProvider, Method foundMethod, Object[] firstParameters) {
        Object[] methodParameters = parametersFactory.buildParameters(parameterProvider, firstParameters, foundMethod);
        return invoke(foundMethod, methodParameters);
    }

    protected <TYPE> TYPE invokeOperation(RepositoryParameterProvider parameterProvider, Method foundMethod, Object[] firstParameters, QueryParams queryParams) {
        Object[] methodParameters = parametersFactory.buildParameters(parameterProvider, firstParameters, foundMethod, queryParams);
        return invoke(foundMethod, methodParameters);
    }

    private <TYPE> TYPE invoke(Method method, Object... args) {
        try {
            return (TYPE) method.invoke(implementationObject, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw (RuntimeException) e.getCause();
        }
    }
}
