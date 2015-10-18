package io.katharsis.resource.registry.repository;

import io.katharsis.repository.ResourceRepository;

import java.io.Serializable;

public class DirectResourceRepositoryEntry<T, ID extends Serializable> implements ResourceRepositoryEntry<T, ID> {
    private final ResourceRepository<T, ID> resourceRepository;

    public DirectResourceRepositoryEntry(ResourceRepository<T, ID> resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public ResourceRepository<T, ?> getResourceRepository() {
        return resourceRepository;
    }
}
