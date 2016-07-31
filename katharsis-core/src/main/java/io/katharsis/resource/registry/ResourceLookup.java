package io.katharsis.resource.registry;

import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.ResourceRepository;

import java.util.Set;

public interface ResourceLookup {

    /**
     * Scans the classpath and builds the registry of resources, repository and relationship repository classes.
     * It does not instantiate them, but it does validate the constraints between them are satisfied.
     * * checks that a repository is always defined for a resource
     * * checks that a relationship is defined for existing resources and both have repositories
     *
     * @param packages
     * @return
     */
    io.katharsis.dispatcher.registry.ResourceRegistry scan(String[] packages);

    @Deprecated
    Set<Class<?>> getResourceClasses();

    /**
     * Returns the repository classes {@link ResourceRepository}, {@link RelationshipRepository}.
     *
     * @return repository classes
     */
    @Deprecated
    Set<Class<?>> getResourceRepositoryClasses();

}
