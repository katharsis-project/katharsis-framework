package io.katharsis.jpa;

import java.io.Serializable;

public class DefaultJpaRepositoryFactory implements JpaRepositoryFactory {

	@Override
	public <T, I extends Serializable> JpaEntityRepository<T, I> createEntityRepository(JpaModule module,
			Class<T> entityClass) {
		return new JpaEntityRepository<>(module, entityClass);
	}

	@Override
	public <T, I extends Serializable, D, J extends Serializable> JpaRelationshipRepository<T, I, D, J> createRelationshipRepository(
			JpaModule module, Class<T> entityClass, Class<D> relatedEntityClass) {
		return new JpaRelationshipRepository<>(module, entityClass, relatedEntityClass);
	}

}
