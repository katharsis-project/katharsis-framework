package io.katharsis.legacy.registry;

import java.io.Serializable;

import io.katharsis.legacy.internal.AnnotatedResourceRepositoryAdapter;
import io.katharsis.legacy.internal.ParametersFactory;
import io.katharsis.legacy.internal.RepositoryMethodParameterProvider;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.resource.registry.ResourceEntry;

public class AnnotatedResourceEntry<T, ID extends Serializable> implements ResourceEntry {
    private final RepositoryInstanceBuilder repositoryInstanceBuilder;
    
    @Deprecated
	private ModuleRegistry moduleRegistry;

    public AnnotatedResourceEntry(ModuleRegistry moduleRegistry, RepositoryInstanceBuilder RepositoryInstanceBuilder) {
		this.moduleRegistry= moduleRegistry;
        this.repositoryInstanceBuilder = RepositoryInstanceBuilder;
    }

    public AnnotatedResourceRepositoryAdapter build(RepositoryMethodParameterProvider parameterProvider) {
        return new AnnotatedResourceRepositoryAdapter<>(repositoryInstanceBuilder.buildRepository(),
            new ParametersFactory(moduleRegistry, parameterProvider));
    }

    @Override
    public String toString() {
        return "AnnotatedResourceEntryBuilder{" +
            "repositoryInstanceBuilder=" + repositoryInstanceBuilder +
            '}';
    }
}
