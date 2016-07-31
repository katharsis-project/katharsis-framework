package io.katharsis.domain.api;

/**
 * An interface that each class that will be used for storing links information in top-level JSON must implement.
 * <p/>
 * Where specified, a links member can be used to represent links.
 * The value of each links member MUST be an object (a “links object”).
 * <p/>
 * Each member of a links object is a “link”. A link MUST be represented as either:
 * * a string containing the link’s URL.
 * * an object (“link object”) which can contain the following members:
 * - href: a string containing the link’s URL.
 * - meta: a meta object containing non-standard meta-information about the link.
 * <p/>
 * http://jsonapi.org/format/#document-links
 */
public interface LinksInformation {
}
