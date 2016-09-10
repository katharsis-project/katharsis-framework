package io.katharsis.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import io.katharsis.jpa.internal.JpaRepositoryUtils;
import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.query.JpaFilterOperators;
import io.katharsis.jpa.query.JpaQuery;
import io.katharsis.jpa.query.JpaQueryExecutor;
import io.katharsis.jpa.query.JpaQueryFactory;
import io.katharsis.queryspec.FilterOperator;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecResourceRepository;

/**
 * Exposes a JPA entity as ResourceRepository.
 */
public class JpaEntityRepository<T, I extends Serializable> implements QuerySpecResourceRepository<T, I> {

	private Class<T> entityType;

	private MetaEntity meta;

	private MetaAttribute primaryKeyAttr;

	private JpaModule module;

	public JpaEntityRepository(JpaModule module, Class<T> entityType) {
		this.entityType = entityType;
		this.meta = module.getMetaLookup().getMeta(entityType).asEntity();
		this.primaryKeyAttr = JpaRepositoryUtils.getPrimaryKeyAttr(meta);
		this.module = module;
	}

	@Override
	public T findOne(I id, QuerySpec querySpec) {
		JpaQueryFactory queryFactory = module.getQueryFactory();
		JpaQuery<T> query = queryFactory.query(entityType);
		JpaRepositoryUtils.prepareQuery(query, querySpec);
		query.addFilter(primaryKeyAttr.getName(), FilterOperator.EQ, id);
		JpaQueryExecutor<T> executor = query.buildExecutor();
		JpaRepositoryUtils.prepareExecutor(executor, querySpec);
		return executor.getUniqueResult(true);
	}

	@Override
	public List<T> findAll(QuerySpec querySpec) {
		JpaQueryFactory queryFactory = module.getQueryFactory();
		JpaQuery<T> query = queryFactory.query(entityType);
		JpaRepositoryUtils.prepareQuery(query, querySpec);
		JpaQueryExecutor<T> executor = query.buildExecutor();
		JpaRepositoryUtils.prepareExecutor(executor, querySpec);
		return executor.getResultList();
	}

	@Override
	public List<T> findAll(Iterable<I> ids, QuerySpec querySpec) {
		JpaQueryFactory queryFactory = module.getQueryFactory();

		ArrayList<I> idList = new ArrayList<>();
		for (I id : ids) {
			idList.add(id);
		}

		JpaQuery<T> query = queryFactory.query(entityType);
		query.addFilter(primaryKeyAttr.getName(), FilterOperator.EQ, ids);

		JpaRepositoryUtils.prepareQuery(query, querySpec);
		JpaQueryExecutor<T> executor = query.buildExecutor();
		JpaRepositoryUtils.prepareExecutor(executor, querySpec);

		return executor.getResultList();
	}

	@Override
	public <S extends T> S save(S entity) {
		EntityManager em = module.getEntityManager();
		em.persist(entity);
		return entity;
	}

	@Override
	public void delete(I id) {
		EntityManager em = module.getEntityManager();

		T object = em.find(entityType, id);
		if (object != null) {
			em.remove(object);
		}
	}

	@Override
	public Class<T> getResourceClass() {
		return entityType;
	}

	@Override
	public Set<FilterOperator> getSupportedOperators() {
		return JpaFilterOperators.getSupportedOperators();
	}

	@Override
	public FilterOperator getDefaultOperator() {
		return JpaFilterOperators.getDefaultOperator();
	}
}
