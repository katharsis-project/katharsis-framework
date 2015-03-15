package io.katharsis.repository;

/**
 * Thrown when repository definition for a resource cannot be found in specified package.
 */
public class RepositoryNotFoundException extends RuntimeException {

    public RepositoryNotFoundException(String message) {
        super(message);
    }
}
