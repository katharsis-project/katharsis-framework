package io.katharsis.repository;

import io.katharsis.queryParams.RequestParams;
import io.katharsis.resource.exception.ResourceNotFoundException;

import java.io.Serializable;

/**
 * Base repository which is used to operate on the resources. Each resource should have a corresponding repository
 * implementation.
 *
 * @param <T>  Type of an entity
 * @param <ID> Type of Identifier of an entity
 */
public interface ResourceRepository<T, ID extends Serializable> {

    /**
     * Search one resource with a given ID. If a resource cannot be found, a {@link ResourceNotFoundException}
     * exception should be thrown.
     *
     * @param id an identifier of the resource
     * @return an instance of the resource
     */
    T findOne(ID id);

    /**
     * Search for all of the resources. An instance of {@link RequestParams} can be used if necessary. If no
     * resources can be found an empty {@link Iterable} or <i>null</i> must be returned.
     *
     * @param requestParams parameters send with the request
     * @return a list of found resources
     */
    Iterable<T> findAll(RequestParams requestParams);

    /**
     * Search for resources constrained by a list of identifiers. An instance of {@link RequestParams} can be used if
     * necessary. If no resources can be found an empty {@link Iterable} or <i>null</i> must be returned.
     *
     * @param ids an {@link Iterable} of passed resource identifiers
     * @param requestParams parameters send with the request
     * @return a list of found resources
     */
    Iterable<T> findAll(Iterable<ID> ids, RequestParams requestParams);

    /**
     * Saves a resource. It should not save relating relationships. A Returning resource must include assigned
     * identifier created for the instance of resource.
     *
     * @param entity resource to be saved
     * @param <S> type of the resource
     * @return saved resource. Must include set identifier.
     */
    <S extends T> S save(S entity);

    /**
     * Removed a resource.
     *
     * @param id identified of the resource to be removed
     */
    void delete(ID id);
}
