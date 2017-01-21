package io.katharsis.legacy.internal;

import java.io.Serializable;
import java.lang.reflect.Method;

import io.katharsis.core.internal.utils.ClassUtils;
import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.legacy.repository.annotations.JsonApiDelete;
import io.katharsis.legacy.repository.annotations.JsonApiFindAll;
import io.katharsis.legacy.repository.annotations.JsonApiFindAllWithIds;
import io.katharsis.legacy.repository.annotations.JsonApiFindOne;
import io.katharsis.legacy.repository.annotations.JsonApiSave;
import io.katharsis.repository.request.QueryAdapter;

/**
 * An adapter for annotation-based resource repository. Stores references to repository methods and call o proper one
 * when a repository method has to be called. This class is instantiated in {@link io.katharsis.legacy.registry.RepositoryInstanceBuilder}
 */
public class AnnotatedResourceRepositoryAdapter<T, ID extends Serializable>
    extends AnnotatedRepositoryAdapter<T> {

    private Method findOneMethod;
    private Method findAllMethod;
    private Method findAllWithIds;
    private Method saveMethod;
    private Method deleteMethod;

    public AnnotatedResourceRepositoryAdapter(Object implementationObject, ParametersFactory parametersFactory) {
        super(implementationObject, parametersFactory);
    }

    public Object findOne(ID id, QueryAdapter queryAdapter) {
        Class<JsonApiFindOne> annotationType = JsonApiFindOne.class;
        if (findOneMethod == null) {
            findOneMethod = ClassUtils.findMethodWith(implementationClass, annotationType);
        }
        return invokeOperation(findOneMethod, annotationType, new Object[]{id}, queryAdapter);
    }

	public Object findAll(QueryAdapter queryAdapter) {
        Class<JsonApiFindAll> annotationType = JsonApiFindAll.class;
        if (findAllMethod == null) {
            findAllMethod = ClassUtils.findMethodWith(implementationClass, annotationType);
        }
        return invokeOperation(findAllMethod, annotationType, new Object[]{}, queryAdapter);
    }

    public Object findAll(Iterable<ID> ids, QueryAdapter queryAdapter) {
        Class<JsonApiFindAllWithIds> annotationType = JsonApiFindAllWithIds.class;
        if (findAllWithIds == null) {
            findAllWithIds = ClassUtils.findMethodWith(implementationClass, annotationType);
        }
        return invokeOperation(findAllWithIds, annotationType, new Object[]{ids}, queryAdapter);
    }

    public <S extends T> Object save(S entity) {
        Class<JsonApiSave> annotationType = JsonApiSave.class;
        if (saveMethod == null) {
            saveMethod = ClassUtils.findMethodWith(implementationClass, annotationType);
        }
        return invokeOperation(saveMethod, annotationType, new Object[]{entity});
    }

    public void delete(ID id, QueryAdapter queryAdapter) {
        Class<JsonApiDelete> annotationType = JsonApiDelete.class;
        if (deleteMethod == null) {
            deleteMethod = ClassUtils.findMethodWith(implementationClass, annotationType);
        }
        invokeOperation(deleteMethod, annotationType, new Object[]{id}, queryAdapter);
    }
}
