package io.katharsis.jpa;

import java.io.Serializable;

public class DefaultJpaRepositoryFactory implements JpaRepositoryFactory {

	@Override
	public <T, T_ID extends Serializable> JpaEntityRepository<T, T_ID> createEntityRepository(JpaModule module,
			Class<T> entityClass) {
		return new JpaEntityRepository<T, T_ID>(module, entityClass);
	}

	@Override
	public <T, T_ID extends Serializable, D, D_ID extends Serializable> JpaRelationshipRepository<T, T_ID, D, D_ID> createRelationshipRepository(
			JpaModule module, Class<T> entityClass, Class<D> relatedEntityClass) {
		return new JpaRelationshipRepository<T, T_ID, D, D_ID>(module, entityClass, relatedEntityClass);
	}

}
