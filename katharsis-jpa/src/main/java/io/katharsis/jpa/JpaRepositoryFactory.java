package io.katharsis.jpa;

import java.io.Serializable;

/**
 * Used to create resource and relationship repositories for the provided
 * classes. By default {@link DefaultJpaRepositoryFactory}} is used.
 */
public interface JpaRepositoryFactory {

	public <T, T_ID extends Serializable> JpaEntityRepository<T, T_ID> createEntityRepository(JpaModule module,
			Class<T> entityClass);

	public <T, T_ID extends Serializable, D, D_ID extends Serializable> JpaRelationshipRepository<T, T_ID, D, D_ID> createRelationshipRepository(
			JpaModule module, Class<T> entityClass, Class<D> relatedEntityClass);
}
