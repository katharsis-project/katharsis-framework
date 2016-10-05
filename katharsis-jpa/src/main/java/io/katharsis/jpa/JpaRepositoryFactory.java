package io.katharsis.jpa;

import java.io.Serializable;

import io.katharsis.jpa.mapping.JpaMapper;

/**
 * Used to create resource and relationship repositories for the provided
 * classes. By default {@link DefaultJpaRepositoryFactory}} is used.
 */
public interface JpaRepositoryFactory {

	/**
	 * Creates a resource repository that maps an entity directly to a JSON API endpoint.
	 * 
	 * @param module
	 * @param entityClass
	 * @return repository
	 */
	public <T, I extends Serializable> JpaEntityRepository<T, I> createEntityRepository(JpaModule module, Class<T> entityClass);

	/**
	 *  Creates a relationship repository that maps an entity relationship directly to a JSON API endpoint.
	 * 
	 * @param module
	 * @param entityClass
	 * @param relatedEntityClass
	 * @return repository
	 */
	public <T, U extends Serializable, D, J extends Serializable> JpaRelationshipRepository<T, U, D, J> createRelationshipRepository(
			JpaModule module, Class<T> entityClass, Class<D> relatedEntityClass);

	/**
	 *  Creates a resource repository that maps entities to DTOs and makes the DTOs available as json api endpoints.
	 * 
	 * @param jpaModule
	 * @param entityClass
	 * @param dtoClass
	 * @param mapper
	 * @return repository
	 */
	public <E, D, I extends Serializable> JpaEntityRepository<D, I> createMappedEntityRepository(JpaModule module,
			Class<E> entityClass, Class<D> dtoClass, JpaMapper<E, D> mapper);

	/**
	 * Creates a relationship repository that maps entity relations to DTO relations and makes those available as json api endpoints.
	 * 
	 * @param module
	 * @param sourceEntityClass
	 * @param targetEntityClass
	 * @param targetDtoClass
	 * @param targetMapper
	 * @return repository
	 */
	public <T, I extends Serializable, D, J extends Serializable, E> JpaRelationshipRepository<T, I, D, J> createMappedRelationshipRepository(
			JpaModule module, Class<T> sourceEntityClass, Class<E> targetEntityClass, Class<D> targetDtoClass,
			JpaMapper<E, D> targetMapper);

}
