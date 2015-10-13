package io.katharsis.resource.registry.repository;

import io.katharsis.repository.ResourceRepository;

/**
 * Created by patryk on 13.10.15.
 */
public class DirectResourceRepositoryEntry<T, ID> implements ResourceRepositoryEntry<T, ID> {
    private final ResourceRepository<T, ?> resourceRepository;

    public DirectResourceRepositoryEntry(ResourceRepository<T, ?> resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public ResourceRepository<T, ?> getResourceRepository() {
        return resourceRepository;
    }
}
