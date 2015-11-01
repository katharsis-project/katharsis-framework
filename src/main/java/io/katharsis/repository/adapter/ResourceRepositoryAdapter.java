package io.katharsis.repository.adapter;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.LinksRepository;
import io.katharsis.repository.MetaRepository;
import io.katharsis.repository.ParametersFactory;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.annotations.*;
import io.katharsis.repository.exception.RepositoryAnnotationNotFoundException;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;
import io.katharsis.utils.ClassUtils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class ResourceRepositoryAdapter<T, ID extends Serializable>
    extends RepositoryAdapter<T>
    implements ResourceRepository<T, ID> {

    private Method findOneMethod;
    private Method findAllMethod;
    private Method findAllWithIds;
    private Method saveMethod;
    private Method deleteMethod;

    public ResourceRepositoryAdapter(Object implementationObject, ParametersFactory parametersFactory) {
        super(implementationObject, parametersFactory);
    }

    @Override
    public T findOne(ID id, QueryParams queryParams) {
        Class<JsonApiFindOne> annotationType = JsonApiFindOne.class;
        if (findOneMethod == null) {
            findOneMethod = ClassUtils.findMethodWith(implementationObject, annotationType);
        }
        checkIfNotNull(annotationType, findOneMethod);

        Object[] methodParameters = parametersFactory
            .buildParameters(new Object[]{id}, findOneMethod.getParameters(), queryParams, annotationType);

        try {
            return (T) findOneMethod.invoke(implementationObject, methodParameters);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw (RuntimeException)e.getCause();
        }
    }

    @Override
    public Iterable<T> findAll(QueryParams queryParams) {
        Class<JsonApiFindAll> annotationType = JsonApiFindAll.class;
        if (findAllMethod == null) {
            findAllMethod = ClassUtils.findMethodWith(implementationObject, annotationType);
        }
        checkIfNotNull(annotationType, findAllMethod);

        Parameter[] parametersToResolve = findAllMethod.getParameters();
        Object[] methodParameters = parametersFactory.buildParameters(parametersToResolve, queryParams);

        try {
            return (Iterable<T>) findAllMethod.invoke(implementationObject, methodParameters);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw (RuntimeException)e.getCause();
        }
    }

    @Override
    public Iterable<T> findAll(Iterable<ID> ids, QueryParams queryParams) {
        Class<JsonApiFindAllWithIds> annotationType = JsonApiFindAllWithIds.class;
        if (findAllWithIds == null) {
            findAllWithIds = ClassUtils.findMethodWith(implementationObject, annotationType);
        }
        checkIfNotNull(annotationType, findAllWithIds);

        Object[] methodParameters = parametersFactory
            .buildParameters(new Object[]{ids}, findAllWithIds.getParameters(), queryParams, annotationType);

        try {
            return (Iterable<T>) findAllWithIds.invoke(implementationObject, methodParameters);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw (RuntimeException)e.getCause();
        }
    }

    @Override
    public <S extends T> S save(S entity) {
        Class<JsonApiSave> annotationType = JsonApiSave.class;
        if (saveMethod == null) {
            saveMethod = ClassUtils.findMethodWith(implementationObject, annotationType);
        }
        checkIfNotNull(annotationType, saveMethod);

        Object[] methodParameters = parametersFactory
            .buildParameters(new Object[]{entity}, saveMethod.getParameters(), annotationType);

        try {
            return (S) saveMethod.invoke(implementationObject, methodParameters);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw (RuntimeException)e.getCause();
        }
    }

    @Override
    public void delete(ID id) {
        Class<JsonApiDelete> annotationType = JsonApiDelete.class;
        if (deleteMethod == null) {
            deleteMethod = ClassUtils.findMethodWith(implementationObject, annotationType);
        }
        checkIfNotNull(annotationType, deleteMethod);

        Object[] methodParameters = parametersFactory
            .buildParameters(new Object[]{id}, deleteMethod.getParameters(), annotationType);

        try {
            deleteMethod.invoke(implementationObject, methodParameters);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw (RuntimeException)e.getCause();
        }
    }
}
