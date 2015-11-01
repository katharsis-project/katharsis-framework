package io.katharsis.resource.registry.repository;

import io.katharsis.repository.ParametersFactory;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.adapter.ResourceRepositoryAdapter;

import java.io.Serializable;

public class AnnotatedResourceEntryBuilder<T, ID extends Serializable> implements ResourceEntry<T, ID> {
    private final Object repositoryImplementation;

    public AnnotatedResourceEntryBuilder(Object repositoryImplementation) {
        this.repositoryImplementation = repositoryImplementation;
    }

    public ResourceRepository<T, ?> build(RepositoryMethodParameterProvider parameterProvider) {
        return new ResourceRepositoryAdapter<>(repositoryImplementation, new ParametersFactory(parameterProvider));
    }
}
