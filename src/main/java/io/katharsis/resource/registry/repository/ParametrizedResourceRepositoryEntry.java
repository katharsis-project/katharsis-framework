package io.katharsis.resource.registry.repository;

import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.ResourceRepositoryFacade;

import java.io.Serializable;

/**
 * Created by patryk on 13.10.15.
 */
public class ParametrizedResourceRepositoryEntry<T, ID extends Serializable> implements ResourceRepositoryEntry<T, ID> {
    private final Object repositoryImplementation;

    public ParametrizedResourceRepositoryEntry(Object repositoryImplementation) {
        this.repositoryImplementation = repositoryImplementation;
    }

    public ResourceRepository<T, ?> buildResourceRepository(RepositoryMethodParameterProvider parameterProvider) {
        return new ResourceRepositoryFacade<>(repositoryImplementation, parameterProvider);
    }
}
