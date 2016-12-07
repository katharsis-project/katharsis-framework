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
	 * @param <T> resource type
	 * @param <I> identifier type
	 * @param module managing the repository
	 * @param config for this repository
	 * @return created repository
	 */
	public <T, I extends Serializable> JpaEntityRepository<T, I> createEntityRepository(JpaModule module,
			JpaRepositoryConfig<T> config);

	/**
	 *  Creates a relationship repository that maps an entity relationship to a JSON API endpoint. The provided resource classes do not necessarily have to be
	 * an entity class. The JpaModule is checked whether there is a mapping available.
	 * 
	 * @param <S> source resource type
	 * @param <I> source identifier type
	 * @param <T> target resource type
	 * @param <J> target identifier type
	 * @param module managing the repository
	 * @param sourceResourceClass representing the source of the relation (entity or mapped dto)
	 * @param config for this repository
	 * @return created repository
	 */
	public <S, I extends Serializable, T, J extends Serializable> JpaRelationshipRepository<S, I, T, J> createRelationshipRepository(
			JpaModule module, Class<S> sourceResourceClass, JpaRepositoryConfig<T> config);

}
