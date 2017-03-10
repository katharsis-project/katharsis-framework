package io.katharsis.legacy.registry;

import io.katharsis.core.internal.utils.ClassUtils;
import io.katharsis.legacy.internal.AnnotatedRelationshipRepositoryAdapter;
import io.katharsis.legacy.internal.ParametersFactory;
import io.katharsis.legacy.internal.RepositoryMethodParameterProvider;
import io.katharsis.legacy.repository.annotations.JsonApiRelationshipRepository;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.resource.registry.ResponseRelationshipEntry;
import io.katharsis.utils.Optional;

public class AnnotatedRelationshipEntryBuilder implements ResponseRelationshipEntry {

    private RepositoryInstanceBuilder repositoryInstanceBuilder;
	private ModuleRegistry moduleRegistry;

    public AnnotatedRelationshipEntryBuilder(ModuleRegistry moduleRegistry, RepositoryInstanceBuilder repositoryInstanceBuilder) {
    	this.moduleRegistry = moduleRegistry;
        this.repositoryInstanceBuilder = repositoryInstanceBuilder;
    }

    @Override
    public Class<?> getTargetAffiliation() {
        @SuppressWarnings("unchecked")
        final Optional<JsonApiRelationshipRepository> annotation = ClassUtils.getAnnotation(
                repositoryInstanceBuilder.getRepositoryClass(),
                JsonApiRelationshipRepository.class
        );

        if (annotation.isPresent()) {
            return annotation.get().target();
        } else {
            throw new IllegalArgumentException(
                    String.format(
                            "Class %s must be annotated with @JsonApiRelationshipRepository",
                            repositoryInstanceBuilder.getClass().getName()
                    )
            );
        }
    }

    public AnnotatedRelationshipRepositoryAdapter build(RepositoryMethodParameterProvider parameterProvider) {
        return new AnnotatedRelationshipRepositoryAdapter<>(repositoryInstanceBuilder.buildRepository(),
            new ParametersFactory(moduleRegistry, parameterProvider));
    }

    @Override
    public String toString() {
        return "AnnotatedRelationshipEntryBuilder{" +
                "repositoryInstanceBuilder=" + repositoryInstanceBuilder +
                '}';
    }
}
