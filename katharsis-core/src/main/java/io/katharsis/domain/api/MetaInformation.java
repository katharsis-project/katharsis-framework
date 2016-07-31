package io.katharsis.domain.api;

/**
 * An interface that each class that will be used for storing meta information in top-level JSON must implement.
 * Where specified, a meta member can be used to include non-standard meta-information.
 * The value of each meta member MUST be an object (a “meta object”).
 * Any members MAY be specified within meta objects.
 * <p/>
 * http://jsonapi.org/format/#document-meta
 */
public interface MetaInformation {
}
