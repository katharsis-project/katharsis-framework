package io.katharsis.repository.adapter;

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

public abstract class RepositoryAdapter<T> implements LinksRepository<T>, MetaRepository<T> {

    protected final Object implementationObject;
    protected final ParametersFactory parametersFactory;

    private Method linksMethod;
    private Method metaMethod;

    public RepositoryAdapter(Object implementationObject, ParametersFactory parametersFactory) {
        this.implementationObject = implementationObject;
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
            .buildParameters(new Object[]{resources}, linksMethod.getParameters(), queryParams, annotationType);

        try {
            return (LinksInformation) linksMethod.invoke(implementationObject, methodParameters);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw (RuntimeException)e.getCause();
        }
    }

    private void assignLinksMethod() {
        if (linksMethod == null) {
            linksMethod = ClassUtils.findMethodWith(implementationObject, JsonApiLinks.class);
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
            .buildParameters(new Object[]{resources}, metaMethod.getParameters(), queryParams, annotationType);

        try {
            return (MetaInformation) metaMethod.invoke(implementationObject, methodParameters);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw (RuntimeException)e.getCause();
        }
    }

    private void assignMetaMethod() {
        if (metaMethod == null) {
            metaMethod = ClassUtils.findMethodWith(implementationObject, JsonApiMeta.class);
        }
    }

    protected void checkIfNotNull(Class<? extends Annotation> annotationClass, Method foundMethod) {
        if (foundMethod == null) {
            throw new RepositoryAnnotationNotFoundException(
                String.format("Annotation %s for class %s not found", annotationClass, implementationObject.getClass()));
        }
    }
}
