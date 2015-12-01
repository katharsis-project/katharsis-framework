package io.katharsis.errorhandling.exception;

/**
 * General type for exceptions, which can be thrown during Katharsis startup (building resource registry etc)
 */
public class KatharsisInitializationException extends RuntimeException {

    protected KatharsisInitializationException(String message) {
        super(message);
    }
    
    protected KatharsisInitializationException(String message, Exception e) {
    	super(message, e);
    }
}
