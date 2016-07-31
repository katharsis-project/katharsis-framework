package io.katharsis.domain.api;

/**
 * Error objects provide additional information about problems encountered while performing an operation.
 * <p/>
 * Error objects MUST be returned as an array keyed by errors in the top level of a JSON API document.
 * <p/>
 * http://jsonapi.org/format/#errors
 */
public interface ErrorObject {

    Object getId();

    LinksInformation getLinks();

    int getStatus();

    String getCode();

    String getTitle();

    String getDetail();

    Object getSource();

    MetaInformation getMeta();

}
