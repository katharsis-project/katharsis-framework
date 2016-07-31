package io.katharsis.dispatcher.registry.api;

import io.katharsis.query.QueryParams;
import io.katharsis.resource.exception.ResourceNotFoundException;

import java.io.Serializable;

public interface Repository<T, ID extends Serializable> {

    /**
     * Search one resource with a given ID. If a resource cannot be found, a {@link ResourceNotFoundException}
     * exception should be thrown.
     *
     * @param id          an identifier of the resource
     * @param queryParams parameters sent along with the request
     * @return an instance of the resource
     */
    T findOne(ID id, QueryParams queryParams);

    /**
     * Search for all of the resources. An instance of {@link QueryParams} can be used if necessary. If no
     * resources can be found, an empty {@link Iterable} or <i>null</i> must be returned.
     *
     * @param queryParams parameters send with the request
     * @return a list of found resources
     */
    Iterable<T> findAll(QueryParams queryParams);

    /**
     * Search for resources constrained by a list of identifiers. An instance of {@link QueryParams} can be used if
     * necessary. If no resources can be found, an empty {@link Iterable} or <i>null</i> must be returned.
     *
     * @param ids         an {@link Iterable} of passed resource identifiers
     * @param queryParams parameters send with the request
     * @return a list of found resources
     */
    Iterable<T> findAll(Iterable<ID> ids, QueryParams queryParams);

    /**
     * Saves a resource. A Returning resource must include assigned identifier created for the instance of resource.
     *
     * @param entity resource to be saved
     * @param <S>    type of the resource
     * @return saved resource. Must include set identifier.
     */
    <S extends T> S save(S entity);

    /**
     * Removes a resource identified by id parameter.
     *
     * @param id identified of the resource to be removed
     */
    void delete(ID id);


}
