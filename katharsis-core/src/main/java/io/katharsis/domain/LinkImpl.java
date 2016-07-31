package io.katharsis.domain;

import io.katharsis.domain.api.MetaInformation;
import lombok.Data;

/**
 * Default link object as defined in the spec.
 * <p/>
 * Example for a "related" link.
 * <p/>
 * "links": {
 * "related": {
 * "href": "http://example.com/articles/1/comments",
 * "meta": {
 * "count": 10
 * }
 * }
 * }
 * <p/>
 * http://jsonapi.org/format/#document-links
 */
@Data
public class LinkImpl {

    private final String href;
    private final MetaInformation meta;

}
