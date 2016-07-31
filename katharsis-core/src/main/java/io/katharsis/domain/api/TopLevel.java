package io.katharsis.domain.api;

/**
 * A JSON object MUST be at the root of every JSON API request and response containing data.
 * <p/>
 * This object defines a document’s “top level”.
 * <p/>
 * A document MUST contain at least one of the following top-level members:
 * * data: the document’s “primary data”
 * * errors: an array of error objects
 * * meta: a meta object that contains non-standard meta-information.
 * <p/>
 * The members data and errors MUST NOT coexist in the same document.
 * <p/>
 * A document MAY contain any of these top-level members:
 * <p/>
 * jsonapi: an object describing the server’s implementation
 * links: a links object related to the primary data.
 * included: an array of resource objects that are related to the primary data and/or each other (“included resources”).
 * <p/>
 * If a document does not contain a top-level data key, the included member MUST NOT be present either.
 */
public interface TopLevel extends ProvidesLinks, ProvidesMeta, ProvidesJsonApi {
}
