package io.katharsis.resource.registry.repository;

import io.katharsis.repository.ParametersFactory;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.ResourceRepositoryAdapter;

import java.io.Serializable;

public class ParametrizedResourceRepositoryEntry<T, ID extends Serializable> implements ResourceRepositoryEntry<T, ID> {
    private final Object repositoryImplementation;

    public ParametrizedResourceRepositoryEntry(Object repositoryImplementation) {
        this.repositoryImplementation = repositoryImplementation;
    }

    public ResourceRepository<T, ?> buildResourceRepository(RepositoryMethodParameterProvider parameterProvider) {
        return new ResourceRepositoryAdapter<>(repositoryImplementation, new ParametersFactory(parameterProvider));
    }
}
