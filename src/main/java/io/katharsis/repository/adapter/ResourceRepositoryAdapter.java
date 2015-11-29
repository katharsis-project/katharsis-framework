package io.katharsis.repository.adapter;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ParametersFactory;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.annotations.*;
import io.katharsis.utils.ClassUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
        return invokeOperation(findOneMethod, annotationType, new Object[]{id}, queryParams);
    }

    @Override
    public Iterable<T> findAll(QueryParams queryParams) {
        Class<JsonApiFindAll> annotationType = JsonApiFindAll.class;
        if (findAllMethod == null) {
            findAllMethod = ClassUtils.findMethodWith(implementationObject, annotationType);
        }
        return invokeOperation(findAllMethod, annotationType, new Object[]{}, queryParams);
    }

    @Override
    public Iterable<T> findAll(Iterable<ID> ids, QueryParams queryParams) {
        Class<JsonApiFindAllWithIds> annotationType = JsonApiFindAllWithIds.class;
        if (findAllWithIds == null) {
            findAllWithIds = ClassUtils.findMethodWith(implementationObject, annotationType);
        }
        return invokeOperation(findAllWithIds, annotationType, new Object[]{ids}, queryParams);
    }

    @Override
    public <S extends T> S save(S entity) {
        Class<JsonApiSave> annotationType = JsonApiSave.class;
        if (saveMethod == null) {
            saveMethod = ClassUtils.findMethodWith(implementationObject, annotationType);
        }
        return invokeOperation(saveMethod, annotationType, new Object[]{entity});
    }

    @Override
    public void delete(ID id) {
        Class<JsonApiDelete> annotationType = JsonApiDelete.class;
        if (deleteMethod == null) {
            deleteMethod = ClassUtils.findMethodWith(implementationObject, annotationType);
        }
        invokeOperation(deleteMethod, annotationType, new Object[]{id});
    }
}
