package io.katharsis.resource.registry.repository;

import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.RepositoryInstanceBuilder;
import lombok.Value;
import net.jodah.typetools.TypeResolver;

@Value
public class DirectResponseRelationshipEntry<T, D> implements ResponseRelationshipEntry<T, D> {

    private final RepositoryInstanceBuilder<RelationshipRepository> repositoryInstanceBuilder;

    @Override
    public Class<?> getTargetAffiliation() {
        Class<?>[] typeArgs = TypeResolver
                .resolveRawArguments(RelationshipRepository.class, repositoryInstanceBuilder.getRepositoryClass());
        return typeArgs[RelationshipRepository.TARGET_TYPE_GENERIC_PARAMETER_IDX];
    }

    public RelationshipRepository getRepositoryInstanceBuilder() {
        return repositoryInstanceBuilder.buildRepository();
    }

}
