package io.katharsis.repository;

import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.annotations.JsonApiDelete;
import io.katharsis.repository.annotations.JsonApiFindAll;
import io.katharsis.repository.annotations.JsonApiFindOne;
import io.katharsis.repository.annotations.JsonApiSave;
import io.katharsis.repository.exception.RepositoryAnnotationNotFoundException;
import io.katharsis.utils.ClassUtils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class ResourceRepositoryAdapter<T, ID extends Serializable> implements ResourceRepository<T, ID> {

    private final Object implementationObject;
    private final ParametersFactory parametersFactory;

    private Method findOneMethod;
    private Method findAllMethod;
    private Method saveMethod;
    private Method deleteMethod;

    public ResourceRepositoryAdapter(Object implementationObject, ParametersFactory parametersFactory) {
        this.implementationObject = implementationObject;
        this.parametersFactory = parametersFactory;
    }

    @Override
    public T findOne(ID id, RequestParams requestParams) {
        Class<JsonApiFindOne> annotationType = JsonApiFindOne.class;
        if (findOneMethod == null) {
            findOneMethod = ClassUtils.findMethodWith(implementationObject, annotationType);
        }
        checkIfNotNull(annotationType, findOneMethod);

        Object[] methodParameters = parametersFactory
            .buildParameters(id, findOneMethod.getParameters(), requestParams, annotationType);

        try {
            return (T) findOneMethod.invoke(implementationObject, methodParameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<T> findAll(RequestParams requestParams) {
        Class<JsonApiFindAll> annotationType = JsonApiFindAll.class;
        if (findAllMethod == null) {
            findAllMethod = ClassUtils.findMethodWith(implementationObject, annotationType);
        }
        checkIfNotNull(annotationType, findAllMethod);

        Parameter[] parametersToResolve = findAllMethod.getParameters();
        Object[] methodParameters = parametersFactory.buildParameters(parametersToResolve, requestParams);

        try {
            return (Iterable<T>) findAllMethod.invoke(implementationObject, methodParameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <S extends T> S save(S entity) {
        Class<JsonApiSave> annotationType = JsonApiSave.class;
        if (saveMethod == null) {
            saveMethod = ClassUtils.findMethodWith(implementationObject, annotationType);
        }
        checkIfNotNull(annotationType, saveMethod);

        Object[] methodParameters = parametersFactory.buildParameters(entity, saveMethod.getParameters(), annotationType);

        try {
            return (S) saveMethod.invoke(implementationObject, methodParameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(ID id) {
        Class<JsonApiDelete> annotationType = JsonApiDelete.class;
        if (deleteMethod == null) {
            deleteMethod = ClassUtils.findMethodWith(implementationObject, annotationType);
        }
        checkIfNotNull(annotationType, deleteMethod);

        Object[] methodParameters = parametersFactory.buildParameters(id, deleteMethod.getParameters(), annotationType);

        try {
            deleteMethod.invoke(implementationObject, methodParameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkIfNotNull(Class<? extends Annotation> annotationClass, Method foundMethod) {
        if (foundMethod == null) {
            throw new RepositoryAnnotationNotFoundException(
                String.format("Annotation %s for class %s not found", annotationClass, implementationObject.getClass()));
        }
    }
}
