package io.katharsis.response;

/**
 * Represents a top-level links object. It has to be included in all top-level responses except links requests which
 * needs additional property - related.
 *
 * @see TopLevelLinksLinks
 */
public class TopLevelLinks {

    private String self;

    public TopLevelLinks(String self) {
        this.self = self;
    }

    public String getSelf() {
        return self;
    }
}
