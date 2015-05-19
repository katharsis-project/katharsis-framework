package io.katharsis.repository;

import io.katharsis.errorHandling.exception.KatharsisInitalizationException;

/**
 * Thrown when repository definition for a resource cannot be found in specified package.
 */
public final class RepositoryNotFoundException extends KatharsisInitalizationException {

    public RepositoryNotFoundException(String missingRepositoryClassName) {
        super("Instance of the repository not found: " + missingRepositoryClassName);
    }
}