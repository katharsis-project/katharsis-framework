package io.katharsis.resource.registry.repository;

import io.katharsis.repository.RepositoryInstanceBuilder;
import io.katharsis.repository.ResourceRepository;
import lombok.Value;

import java.io.Serializable;

@Value
public class DirectResponseResourceEntry<T, ID extends Serializable> implements ResourceEntry<T, ID> {

    private final RepositoryInstanceBuilder<ResourceRepository<T, ID>> repositoryInstanceBuilder;

    public ResourceRepository<T, ?> getResourceRepository() {
        return repositoryInstanceBuilder.buildRepository();
    }

}
