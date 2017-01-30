package io.katharsis.core.internal.registry;

import io.katharsis.legacy.registry.RepositoryInstanceBuilder;
import io.katharsis.legacy.repository.RelationshipRepository;
import io.katharsis.repository.RelationshipRepositoryV2;
import io.katharsis.resource.registry.ResponseRelationshipEntry;
import net.jodah.typetools.TypeResolver;

public class DirectResponseRelationshipEntry implements ResponseRelationshipEntry {

    private RepositoryInstanceBuilder<RelationshipRepository> repositoryInstanceBuilder;

    public DirectResponseRelationshipEntry(RepositoryInstanceBuilder repositoryInstanceBuilder) {
        this.repositoryInstanceBuilder = repositoryInstanceBuilder;
    }

    @Override
    public Class<?> getTargetAffiliation() {
    	Class<?> repoClass = repositoryInstanceBuilder.getRepositoryClass();
    	Class<?> repoInterface = RelationshipRepositoryV2.class.isAssignableFrom(repoClass) ? RelationshipRepositoryV2.class : RelationshipRepository.class; 
    	
        Class<?>[] typeArgs = TypeResolver
            .resolveRawArguments(repoInterface, repoClass);
        
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
