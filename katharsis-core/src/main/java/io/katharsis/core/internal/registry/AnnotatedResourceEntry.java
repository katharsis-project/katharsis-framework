package io.katharsis.core.internal.registry;

import java.io.Serializable;

import io.katharsis.core.internal.repository.adapter.AnnotatedResourceRepositoryAdapter;
import io.katharsis.repository.ParametersFactory;
import io.katharsis.repository.RepositoryInstanceBuilder;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.resource.registry.ResourceEntry;
import io.katharsis.resource.registry.ResourceRegistry;

public class AnnotatedResourceEntry<T, ID extends Serializable> implements ResourceEntry<T, ID> {
    private final RepositoryInstanceBuilder repositoryInstanceBuilder;

    public AnnotatedResourceEntry(RepositoryInstanceBuilder RepositoryInstanceBuilder) {
        this.repositoryInstanceBuilder = RepositoryInstanceBuilder;
    }

    public AnnotatedResourceRepositoryAdapter build(RepositoryMethodParameterProvider parameterProvider) {
        return new AnnotatedResourceRepositoryAdapter<>(repositoryInstanceBuilder.buildRepository(),
            new ParametersFactory(parameterProvider));
    }

    @Override
    public String toString() {
        return "AnnotatedResourceEntryBuilder{" +
            "repositoryInstanceBuilder=" + repositoryInstanceBuilder +
            '}';
    }
}
