package io.katharsis.jpa;

import java.io.Serializable;

import io.katharsis.jpa.mapping.JpaMapper;

public class DefaultJpaRepositoryFactory implements JpaRepositoryFactory {

	@Override
	public <T, I extends Serializable> JpaEntityRepository<T, I> createEntityRepository(JpaModule module, Class<T> entityClass) {
		return new JpaEntityRepository<>(module, entityClass);
	}

	@Override
	public <T, I extends Serializable, D, J extends Serializable> JpaRelationshipRepository<T, I, D, J> createRelationshipRepository(
			JpaModule module, Class<T> entityClass, Class<D> relatedEntityClass) {
		return new JpaRelationshipRepository<>(module, entityClass, relatedEntityClass);
	}

	@Override
	public <E, D, I extends Serializable> JpaEntityRepository<D, I> createMappedEntityRepository(JpaModule module,
			Class<E> entityClass, Class<D> dtoClass, JpaMapper<E, D> mapper) {
		return new JpaEntityRepository<>(module, entityClass, dtoClass, mapper);
	}

	@Override
	public <S, I extends Serializable, T, J extends Serializable, E, F> JpaRelationshipRepository<S, I, T, J> createMappedRelationshipRepository(
			JpaModule module, Class<E> sourceEntityClass, Class<S> sourceResourceClass, Class<F> targetEntityClass,
			Class<T> targetResourceClass, JpaMapper<E, S> sourceMapper, JpaMapper<F, T> targetMapper) {
		return new JpaRelationshipRepository<>(module, sourceEntityClass, sourceResourceClass, targetEntityClass,
				targetResourceClass, sourceMapper, targetMapper);
	}

}
