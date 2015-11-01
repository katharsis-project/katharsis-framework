package io.katharsis.resource.registry.repository;

import io.katharsis.repository.*;
import io.katharsis.repository.adapter.RelationshipRepositoryAdapter;
import io.katharsis.repository.annotations.JsonApiRelationshipRepository;

public class AnnotatedRelationshipEntryBuilder<T, D> implements RelationshipEntry<T, D> {

    private Object repositoryInstance;

    public AnnotatedRelationshipEntryBuilder(Object repositoryInstance) {
        this.repositoryInstance = repositoryInstance;
    }

    @Override
    public Class<?> getTargetAffiliation() {
        return repositoryInstance.getClass()
            .getAnnotation(JsonApiRelationshipRepository.class)
            .target();
    }

    public RelationshipRepository<T, ?, ?, ?> build(RepositoryMethodParameterProvider parameterProvider) {
        return new RelationshipRepositoryAdapter<>(repositoryInstance, new ParametersFactory(parameterProvider));
    }
}
