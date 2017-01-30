package io.katharsis.errorhandling.exception;

/**
 * Thrown when repository definition for a resource cannot be found in specified package.
 */
public final class RepositoryNotFoundException extends KatharsisMatchingException {
    public RepositoryNotFoundException(Class clazz) {
        super("Repository for a resource not found: " + clazz.getCanonicalName());
    }
    
    public RepositoryNotFoundException(String resourceType) {
        super("Repository for a resource not found: " + resourceType);
    }
}
