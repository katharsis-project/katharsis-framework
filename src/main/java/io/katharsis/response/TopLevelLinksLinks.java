package io.katharsis.response;

/**
 * Represents a top-level links object. It has to be included in all top-level responses which needs additional
 * property - related.
 *
 * @see TopLevelLinks
 */
public class TopLevelLinksLinks extends TopLevelLinks {

    private String related;

    public TopLevelLinksLinks(String self, String related) {
        super(self);
        this.related = related;
    }

    public String getRelated() {
        return related;
    }
}
