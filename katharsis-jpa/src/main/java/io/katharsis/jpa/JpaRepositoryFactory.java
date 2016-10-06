package io.katharsis.jpa;

import java.io.Serializable;

/**
 * Used to create resource and relationship repositories for the provided
 * classes. By default {@link DefaultJpaRepositoryFactory}} is used.
 */
public interface JpaRepositoryFactory {

	/**
	 * Creates a resource repository that maps an entity to a JSON API endpoint. The provided resource class not necessarily has to be
	 * an entity class. The JpaModule is checked whether there is a mapping available.
	 * 
	 * @param module
	 * @param resourceClass
	 * @return repository
	 */
	public <T, I extends Serializable> JpaEntityRepository<T, I> createEntityRepository(JpaModule module, Class<T> resourceClass);

	/**
	 *  Creates a relationship repository that maps an entity relationship to a JSON API endpoint. The provided resource classes do not necessarily have to be
	 * an entity class. The JpaModule is checked whether there is a mapping available.
	 * 
	 * @param module
	 * @param sourceResourceClass
	 * @param targetResourceClass
	 * @return repository
	 */
	public <S, U extends Serializable, T, J extends Serializable> JpaRelationshipRepository<S, U, T, J> createRelationshipRepository(
			JpaModule module, Class<S> sourceResourceClass, Class<T> targetResourceClass);

		

}
