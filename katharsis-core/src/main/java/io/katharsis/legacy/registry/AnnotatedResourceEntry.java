package io.katharsis.legacy.registry;

import java.io.Serializable;

import io.katharsis.legacy.internal.AnnotatedResourceRepositoryAdapter;
import io.katharsis.legacy.internal.ParametersFactory;
import io.katharsis.legacy.internal.RepositoryMethodParameterProvider;
import io.katharsis.resource.registry.ResourceEntry;
import io.katharsis.resource.registry.ResourceRegistry;

public class AnnotatedResourceEntry<T, ID extends Serializable> implements ResourceEntry {
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
