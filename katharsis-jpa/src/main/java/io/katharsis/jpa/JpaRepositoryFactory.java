package io.katharsis.jpa;

import java.io.Serializable;

/**
 * Used to create resource and relationship repositories for the provided
 * classes. By default {@link DefaultJpaRepositoryFactory}} is used.
 */
public interface JpaRepositoryFactory {

	public <T, I extends Serializable> JpaEntityRepository<T, I> createEntityRepository(JpaModule module,
			Class<T> entityClass);

	public <T, U extends Serializable, D, J extends Serializable> JpaRelationshipRepository<T, U, D, J> createRelationshipRepository(
			JpaModule module, Class<T> entityClass, Class<D> relatedEntityClass);
}
