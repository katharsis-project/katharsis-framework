package io.katharsis.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.katharsis.domain.api.JsonApi;
import io.katharsis.domain.api.LinksInformation;
import io.katharsis.domain.api.MetaInformation;
import io.katharsis.domain.api.TopLevel;
import lombok.Data;

import javax.annotation.Nullable;

/**
 * A JSON object MUST be at the root of every JSON API request and response containing data. This object defines a document’s “top level”.
 * <p/>
 * A document MUST contain at least one of the following top-level members:
 * - data: the document’s “primary data”
 * - errors: an array of error objects
 * - meta: a meta object that contains non-standard meta-information.
 * <p/>
 * http://jsonapi.org/format/#document-top-level
 */
@Data
public class MetaResponse implements TopLevel {

    private MetaInformation meta;
    @JsonProperty("jsonapi")
    private JsonApi jsonApi;
    private LinksInformation links;

    public MetaResponse(MetaInformation meta,
                        @Nullable JsonApi jsonApi,
                        @Nullable LinksInformation links) {
        this.meta = meta;
        this.jsonApi = jsonApi;
        this.links = links;
    }
}

