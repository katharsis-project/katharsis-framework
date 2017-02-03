package io.katharsis.errorhandling.exception;

public class InvalidResourceException extends KatharsisInitializationException {

    public InvalidResourceException(String message) {
        super(message);
    }
    
    public InvalidResourceException(String message, Exception e) {
    	super(message, e);
    }
}
