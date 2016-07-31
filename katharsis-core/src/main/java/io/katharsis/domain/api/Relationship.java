package io.katharsis.domain.api;

import javax.annotation.Nullable;

/**
 * The value of the relationships key MUST be an object (a “relationships object”).
 * Members of the relationships object (“relationships”) represent references from the resource object
 * in which it’s defined to other resource objects.
 * <p/>
 * http://jsonapi.org/format/#document-resource-object-relationships
 */
public interface Relationship extends ProvidesMeta, ProvidesLinks {

    /**
     * Resource linkage.
     * <p/>
     * http://jsonapi.org/format/#document-resource-object-linkage
     *
     * @param <T> Resource linkage.
     * @return
     */
    @Nullable
    <T> T getData();

}
