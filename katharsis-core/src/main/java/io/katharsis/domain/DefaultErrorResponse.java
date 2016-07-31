package io.katharsis.domain;

import io.katharsis.domain.api.ErrorObject;
import io.katharsis.domain.api.ErrorResponse;
import io.katharsis.domain.api.JsonApi;
import io.katharsis.domain.api.LinksInformation;
import io.katharsis.domain.api.MetaInformation;
import lombok.Data;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Primary data MUST be either:
 * <p/>
 * - a single resource object, a single resource identifier object, or null, for requests that target single resources
 * - an array of resource objects, an array of resource identifier objects, or an empty array ([]), for requests that target resource collections
 * <p/>
 * http://jsonapi.org/format/#document-top-level
 */
@Data
public class DefaultErrorResponse implements ErrorResponse {

    private Collection<ErrorObject> errors;
    private MetaInformation meta;
    private JsonApi jsonApi;
    private LinksInformation links;

    public DefaultErrorResponse(Collection<ErrorObject> errors,
                                @Nullable MetaInformation meta,
                                @Nullable JsonApi jsonApi,
                                @Nullable LinksInformation links) {
        this.meta = meta;
        this.jsonApi = jsonApi;
        this.links = links;
        this.errors = errors;
    }
}
