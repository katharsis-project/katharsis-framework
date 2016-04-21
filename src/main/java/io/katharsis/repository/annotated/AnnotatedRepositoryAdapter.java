package io.katharsis.repository.annotated;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.LinksRepository;
import io.katharsis.repository.MetaRepository;
import io.katharsis.repository.ParametersFactory;
import io.katharsis.repository.annotations.JsonApiLinks;
import io.katharsis.repository.annotations.JsonApiMeta;
import io.katharsis.repository.exception.RepositoryAnnotationNotFoundException;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;
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
    }

    public boolean linksRepositoryAvailable() {
        assignLinksMethod();
        return linksMethod != null;
    }

    @Override
    public LinksInformation getLinksInformation(Iterable<T> resources, QueryParams queryParams) {
        Class<JsonApiLinks> annotationType = JsonApiLinks.class;
        assignLinksMethod();
        checkIfNotNull(annotationType, linksMethod);

        Object[] methodParameters = parametersFactory
            .buildParameters(new Object[]{resources}, linksMethod, queryParams, annotationType);

        return invoke(linksMethod, methodParameters);
    }

    private void assignLinksMethod() {
        if (linksMethod == null) {
            linksMethod = ClassUtils.findMethodWith(implementationClass, JsonApiLinks.class);
        }
    }

    public boolean metaRepositoryAvailable() {
        assignMetaMethod();
        return metaMethod != null;
    }

    @Override
    public MetaInformation getMetaInformation(Iterable<T> resources, QueryParams queryParams) {
        Class<JsonApiMeta> annotationType = JsonApiMeta.class;
        assignMetaMethod();
        checkIfNotNull(annotationType, metaMethod);

        Object[] methodParameters = parametersFactory
            .buildParameters(new Object[]{resources}, metaMethod, queryParams, annotationType);

        return invoke(metaMethod, methodParameters);
    }

    private void assignMetaMethod() {
        if (metaMethod == null) {
            metaMethod = ClassUtils.findMethodWith(implementationClass, JsonApiMeta.class);
        }
    }

    protected void checkIfNotNull(Class<? extends Annotation> annotationClass, Method foundMethod) {
        if (foundMethod == null) {
            throw new RepositoryAnnotationNotFoundException(
                String.format("Annotation %s for class %s not found", annotationClass, implementationObject.getClass()));
        }
    }

    protected <TYPE> TYPE invokeOperation(Method foundMethod, Class<? extends Annotation> annotationType,
                                          Object[] firstParameters) {
        checkIfNotNull(annotationType, foundMethod);
        Object[] methodParameters = parametersFactory
            .buildParameters(firstParameters, foundMethod, annotationType);
        return invoke(foundMethod, methodParameters);
    }

    protected <TYPE> TYPE invokeOperation(Method foundMethod, Class<? extends Annotation> annotationType,
                                          Object[] firstParameters, QueryParams queryParams) {
        checkIfNotNull(annotationType, foundMethod);
        Object[] methodParameters = parametersFactory
            .buildParameters(firstParameters, foundMethod, queryParams, annotationType);
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
