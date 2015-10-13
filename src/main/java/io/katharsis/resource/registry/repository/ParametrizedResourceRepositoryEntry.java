package io.katharsis.resource.registry.repository;

import io.katharsis.repository.ResourceMethodParameterProvider;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.ResourceRepositoryFacade;

/**
 * Created by patryk on 13.10.15.
 */
public class ParametrizedResourceRepositoryEntry<T, ID> implements ResourceRepositoryEntry<T, ID> {
    private final Object repositoryImplementation;

    public ParametrizedResourceRepositoryEntry(Object repositoryImplementation) {
        this.repositoryImplementation = repositoryImplementation;
    }

    public ResourceRepository<T, ?> buildResourceRepository(ResourceMethodParameterProvider parameterProvider) {
        return new ResourceRepositoryFacade<>(repositoryImplementation, parameterProvider);
    }
}
