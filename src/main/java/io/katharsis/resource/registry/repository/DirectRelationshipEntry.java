package io.katharsis.resource.registry.repository;

import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.RepositoryInstanceBuilder;
import net.jodah.typetools.TypeResolver;

public class DirectRelationshipEntry<T, D> implements RelationshipEntry<T, D> {

    private RepositoryInstanceBuilder<RelationshipRepository> repositoryInstanceBuilder;

    public DirectRelationshipEntry(RepositoryInstanceBuilder<RelationshipRepository> repositoryInstanceBuilder) {
        this.repositoryInstanceBuilder = repositoryInstanceBuilder;
    }

    @Override
    public Class<?> getTargetAffiliation() {
        Class<?>[] typeArgs = TypeResolver
            .resolveRawArguments(RelationshipRepository.class, repositoryInstanceBuilder.getRepositoryClass());
        return typeArgs[RelationshipRepository.TARGET_TYPE_GENERIC_PARAMETER_IDX];
    }

    public RelationshipRepository getRepositoryInstanceBuilder() {
        return repositoryInstanceBuilder.buildRepository();
    }

    @Override
    public String toString() {
        return "DirectRelationshipEntry{" +
            "repositoryInstanceBuilder=" + repositoryInstanceBuilder +
            '}';
    }
}
