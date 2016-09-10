package io.katharsis.resource.registry.repository;

import io.katharsis.queryspec.QuerySpecRelationshipRepository;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.RepositoryInstanceBuilder;
import net.jodah.typetools.TypeResolver;

public class DirectResponseRelationshipEntry<T, D> implements ResponseRelationshipEntry<T, D> {

    private RepositoryInstanceBuilder<RelationshipRepository> repositoryInstanceBuilder;

    public DirectResponseRelationshipEntry(RepositoryInstanceBuilder<RelationshipRepository> repositoryInstanceBuilder) {
        this.repositoryInstanceBuilder = repositoryInstanceBuilder;
    }

    @Override
    public Class<?> getTargetAffiliation() {
        Class<?>[] typeArgs = TypeResolver
            .resolveRawArguments(RelationshipRepository.class, repositoryInstanceBuilder.getRepositoryClass());
        
        if(typeArgs == null){
        	typeArgs = TypeResolver
                    .resolveRawArguments(QuerySpecRelationshipRepository.class, repositoryInstanceBuilder.getRepositoryClass());
        }
        
        return typeArgs[RelationshipRepository.TARGET_TYPE_GENERIC_PARAMETER_IDX];
    }

    public Object getRepositoryInstanceBuilder() {
        return repositoryInstanceBuilder.buildRepository();
    }

    @Override
    public String toString() {
        return "DirectResponseRelationshipEntry{" +
            "repositoryInstanceBuilder=" + repositoryInstanceBuilder +
            '}';
    }
}
