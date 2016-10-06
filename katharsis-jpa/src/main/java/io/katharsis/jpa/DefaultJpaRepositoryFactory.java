package io.katharsis.jpa;

import java.io.Serializable;

public class DefaultJpaRepositoryFactory implements JpaRepositoryFactory {

	@Override
	public <T, I extends Serializable> JpaEntityRepository<T, I> createEntityRepository(JpaModule module, Class<T> resourceClass) {
		return new JpaEntityRepository<>(module, resourceClass);
	}

	@Override
	public <T, I extends Serializable, D, J extends Serializable> JpaRelationshipRepository<T, I, D, J> createRelationshipRepository(
			JpaModule module, Class<T> sourceResourceClass, Class<D> targetResourceClass) {
		return new JpaRelationshipRepository<>(module, sourceResourceClass, targetResourceClass);
	}
}
