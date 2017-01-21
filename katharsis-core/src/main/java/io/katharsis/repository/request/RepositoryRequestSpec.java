package io.katharsis.repository.request;

import java.io.Serializable;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.internal.QueryAdapter;

/**
 * Information about the current request.
 */
public interface RepositoryRequestSpec {

	
	/**
	 * @return http method used
	 */
	HttpMethod getMethod();
	
	/**
	 * @return issued query
	 */
	QueryAdapter getQueryAdapter();

	/**
	 * @param targetResourceClass to base the QuerySpec upon. Usually the requested resource, but may also be the type of one of the relations.
	 * @return issued query as QuerySpec
	 */
	QuerySpec getQuerySpec(Class<?> targetResourceClass);

	/**
	 * @return issued query as QueryParams
	 */
	QueryParams getQueryParams();

	/**
	 * @return name of relationship field that is involved in the request or null.
	 */
	String getRelationshipField();

	/**
	 * @return type of the relationship that is fetched or null otherwise. If the returned type is non-null, {@link #getRelationshipField} returns the involved relationship field.
	 */
	Class<?> getRelationshipSourceClass();

	/**
	 * @return involved entity for push and patch operations or null otherwise.
	 */
	Object getEntity();

	/**
	 * @return involved id or null if not available. For example the id of the resource to be deleted or from which to fetch relations.  
	 */
	Serializable getId();

	/**
	 * @return involved id or null if not available.
	 */
	<T> Iterable<T> getIds();
}