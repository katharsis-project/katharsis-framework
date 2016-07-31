package io.katharsis.resource.registry.repository;

import io.katharsis.repository.ParametersFactory;
import io.katharsis.repository.RepositoryInstanceBuilder;
import io.katharsis.repository.RepositoryParameterProvider;
import io.katharsis.repository.annotated.AnnotatedResourceRepositoryAdapter;
import lombok.Value;

import java.io.Serializable;

@Value
public class AnnotatedResourceEntryBuilder<T, ID extends Serializable> implements ResourceEntry<T, ID> {

    private final RepositoryInstanceBuilder repositoryInstanceBuilder;

    public AnnotatedResourceRepositoryAdapter build(RepositoryParameterProvider parameterProvider) {
        return new AnnotatedResourceRepositoryAdapter<>(repositoryInstanceBuilder.buildRepository(),
                new ParametersFactory(parameterProvider));
    }

}
