package io.katharsis.errorhandling.exception;

/**
 * Thrown when repository instance for a resource cannot be found
 */
public final class RepositoryInstanceNotFoundException extends KatharsisMatchingException {

    public RepositoryInstanceNotFoundException(String missingRepositoryClassName) {
        super("Instance of the repository not found: " + missingRepositoryClassName);
    }
}