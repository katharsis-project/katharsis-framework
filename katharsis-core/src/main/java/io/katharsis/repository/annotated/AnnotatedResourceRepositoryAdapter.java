package io.katharsis.repository.annotated;

import java.io.Serializable;
import java.lang.reflect.Method;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.repository.ParametersFactory;
import io.katharsis.repository.annotations.JsonApiDelete;
import io.katharsis.repository.annotations.JsonApiFindAll;
import io.katharsis.repository.annotations.JsonApiFindAllWithIds;
import io.katharsis.repository.annotations.JsonApiFindOne;
import io.katharsis.repository.annotations.JsonApiSave;
import io.katharsis.utils.ClassUtils;

/**
 * An adapter for annotation-based resource repository. Stores references to repository methods and call o proper one
 * when a repository method has to be called. This class is instantiated in {@link io.katharsis.repository.RepositoryInstanceBuilder}
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
