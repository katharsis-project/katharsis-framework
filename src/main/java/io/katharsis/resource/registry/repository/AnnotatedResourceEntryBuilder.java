package io.katharsis.resource.registry.repository;

import io.katharsis.repository.ParametersFactory;
import io.katharsis.repository.RepositoryInstanceBuilder;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.adapter.ResourceRepositoryAdapter;

import java.io.Serializable;

public class AnnotatedResourceEntryBuilder<T, ID extends Serializable> implements ResourceEntry<T, ID> {
    private final RepositoryInstanceBuilder repositoryInstanceBuilder;

    public AnnotatedResourceEntryBuilder(RepositoryInstanceBuilder RepositoryInstanceBuilder) {
        this.repositoryInstanceBuilder = RepositoryInstanceBuilder;
    }

    public ResourceRepository<T, ?> build(RepositoryMethodParameterProvider parameterProvider) {
        return new ResourceRepositoryAdapter<>(repositoryInstanceBuilder.buildRepository(),
            new ParametersFactory(parameterProvider));
    }

    @Override
    public String toString() {
        return "AnnotatedResourceEntryBuilder{" +
            "repositoryInstanceBuilder=" + repositoryInstanceBuilder +
            '}';
    }
}
