package io.katharsis.dispatcher.registry.annotated;

import io.katharsis.errorhandling.exception.KatharsisInitializationException;
import io.katharsis.query.QueryParams;
import io.katharsis.repository.RepositoryParameterProvider;
import io.katharsis.repository.annotations.JsonApiDelete;
import io.katharsis.repository.annotations.JsonApiFindAll;
import io.katharsis.repository.annotations.JsonApiFindAllWithIds;
import io.katharsis.repository.annotations.JsonApiFindOne;
import io.katharsis.repository.annotations.JsonApiResourceRepository;
import io.katharsis.repository.annotations.JsonApiSave;
import io.katharsis.utils.ClassUtils;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * An adapter for annotation-based resource repository. Stores references to repository methods and call o proper one
 * when a repository method has to be called. This class is instantiated in {@link io.katharsis.repository.RepositoryInstanceBuilder}
 */
public class AnnotatedResourceRepositoryAdapter<T, ID extends Serializable> extends AnnotatedRepositoryAdapter<T> {

    private Method findOneMethod;
    private Method findAllMethod;
    private Method findAllWithIds;
    private Method saveMethod;
    private Method deleteMethod;

    public AnnotatedResourceRepositoryAdapter(Object implementationObject, ParametersFactory parametersFactory) {
        super(implementationObject, parametersFactory);

        JsonApiResourceRepository annotation = implementationObject.getClass().getAnnotation(JsonApiResourceRepository.class);
        if (annotation == null) {
            throw new KatharsisInitializationException("Repository annotation (JsonApiResourceRepository) is missing from " + implementationObject);
        }

        findOneMethod = ClassUtils.findMethodWith(implementationClass, JsonApiFindOne.class);
        findAllMethod = ClassUtils.findMethodWith(implementationClass, JsonApiFindAll.class);
        findAllWithIds = ClassUtils.findMethodWith(implementationClass, JsonApiFindAllWithIds.class);
        saveMethod = ClassUtils.findMethodWith(implementationClass, JsonApiSave.class);
        deleteMethod = ClassUtils.findMethodWith(implementationClass, JsonApiDelete.class);
    }

    public Object findOne(RepositoryParameterProvider parameterProvider, ID id, QueryParams queryParams) {
        checkIfNotNull(findOneMethod, JsonApiFindOne.class);
        return invokeOperation(parameterProvider, findOneMethod, new Object[]{id}, queryParams);
    }

    public Object findAll(RepositoryParameterProvider parameterProvider, QueryParams queryParams) {
        checkIfNotNull(findAllMethod, JsonApiFindAll.class);
        return invokeOperation(parameterProvider, findAllMethod, new Object[]{}, queryParams);
    }

    public Object findAll(RepositoryParameterProvider parameterProvider, Iterable<ID> ids, QueryParams queryParams) {
        checkIfNotNull(findAllWithIds, JsonApiFindAllWithIds.class);
        return invokeOperation(parameterProvider, findAllWithIds, new Object[]{ids}, queryParams);
    }

    public <S extends T> Object save(RepositoryParameterProvider parameterProvider, S entity) {
        checkIfNotNull(saveMethod, JsonApiSave.class);
        return invokeOperation(parameterProvider, saveMethod, new Object[]{entity});
    }

    public void delete(RepositoryParameterProvider parameterProvider, ID id, QueryParams queryParams) {
        checkIfNotNull(deleteMethod, JsonApiDelete.class);
        invokeOperation(parameterProvider, deleteMethod, new Object[]{id}, queryParams);
    }
}
