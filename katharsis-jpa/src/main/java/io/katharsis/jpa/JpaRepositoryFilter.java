package io.katharsis.jpa;

import java.io.Serializable;
import java.util.List;

import io.katharsis.jpa.query.JpaQuery;
import io.katharsis.jpa.query.JpaQueryExecutor;
import io.katharsis.jpa.query.Tuple;
import io.katharsis.queryspec.QuerySpec;

/**
 * Can be registered with the JpaModule and gets notified about all kinds of repository events.
 * The filter then has to possiblity to do all kinds of changes.
 */
public interface JpaRepositoryFilter {

	/**
	 * Called when repository is created. Allows customizations and replacement.
	 * 
	 * @param repository
	 * @param filtered repository 
	 */
	<T, I extends Serializable> JpaEntityRepository<T, I> filterCreation(JpaEntityRepository<T, I> repository);

	/**
	 * Called when repository is created. Allows customizations and replacement.
	 * 
	 * @param repository
	 * @param filtered repository 
	 */
	<S, I extends Serializable, T, J extends Serializable> JpaRelationshipRepository<S, I, T, J> filterCreation(
			JpaRelationshipRepository<S, I, T, J> repository);

	/**
	 * Specifies whether any of the filter methods should be executed for the given resourceType.;
	 * 
	 * @param resourceType
	 * @return true if filter should be used for the given resouceType.
	 */
	boolean accept(Class<?> resourceType);

	/**
	 * Allows to customize the querySpec before creating the query.
	 * 
	 * @param repository
	 * @param querySpec
	 */
	QuerySpec filterQuerySpec(Object repository, QuerySpec querySpec);

	/**
	 * Allows to customize the query.
	 * 
	 * @param repository
	 * @param querySpec
	 * @param query
	 */
	<T> JpaQuery<T> filterQuery(Object repository, QuerySpec querySpec, JpaQuery<T> query);

	/**
	 * Allows to customize the query executor.
	 * 
	 * @param repository
	 * @param querySpec
	 * @param executor
	 */
	<T> JpaQueryExecutor<T> filterExecutor(Object repository, QuerySpec querySpec, JpaQueryExecutor<T> executor);

	/**
	 * Allows to filter tuples and return the filtered slistet.
	 * 
	 * @param repository
	 * @param querySpec
	 * @param tuples
	 * @return filtered list of tuples
	 */
	List<Tuple> filterTuples(Object repository, QuerySpec querySpec, List<Tuple> tuples);

	/**
	 *  Allows to filter resources and return the filtered list.
	 *  
	 * @param repository
	 * @param querySpec
	 * @param resources
	 * @return filtered list of resources
	 */
	<T> List<T> filterResults(Object repository, QuerySpec querySpec, List<T> resources);

}
