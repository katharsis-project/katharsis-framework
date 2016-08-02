package io.katharsis.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import io.katharsis.jpa.internal.JpaRepositoryUtils;
import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.internal.query.FilterOperator;
import io.katharsis.jpa.internal.query.QueryBuilder;
import io.katharsis.jpa.internal.query.QueryBuilderFactory;
import io.katharsis.jpa.internal.query.QueryExecutor;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ResourceRepository;

/**
 * Exposes a JPA entity as ResourceRepository.
 */
public class JpaEntityRepository<T, ID extends Serializable> implements ResourceRepository<T, ID> {

	private Class<T> entityType;
	private MetaEntity meta;
	private MetaAttribute primaryKeyAttr;
	private JpaModule module;

	public JpaEntityRepository(JpaModule module, Class<T> entityType) {
		this.entityType = entityType;
		this.meta = MetaLookup.INSTANCE.getMeta(entityType).asEntity();
		this.primaryKeyAttr = JpaRepositoryUtils.getPrimaryKeyAttr(meta);
		this.module = module;
	}

	@Override
	public T findOne(ID id, QueryParams queryParams) {
		QueryParamsProcessor processor = module.getProcessor();
		QueryBuilderFactory queryBuilderFactory = module.getQueryBuilderFactory();

		QueryBuilder<T> builder = queryBuilderFactory.newBuilder(entityType);
		builder.addFilter(primaryKeyAttr.getName(), FilterOperator.EQUAL, id);
		processor.prepareQuery(builder, queryParams);

		QueryExecutor<T> executor = builder.buildExecutor();
		processor.prepareExecution(executor, queryParams);

		T entity = executor.getUniqueResult(true);
		return entity;
	}

	@Override
	public List<T> findAll(QueryParams queryParams) {
		QueryParamsProcessor processor = module.getProcessor();
		QueryBuilderFactory queryBuilderFactory = module.getQueryBuilderFactory();

		QueryBuilder<T> builder = queryBuilderFactory.newBuilder(entityType);
		processor.prepareQuery(builder, queryParams);

		QueryExecutor<T> executor = builder.buildExecutor();
		processor.prepareExecution(executor, queryParams);

		List<T> list = executor.getResultList();
		return list;
	}

	@Override
	public List<T> findAll(Iterable<ID> ids, QueryParams queryParams) {
		QueryParamsProcessor processor = module.getProcessor();
		QueryBuilderFactory queryBuilderFactory = module.getQueryBuilderFactory();

		ArrayList<ID> idList = new ArrayList<ID>();
		for (ID id : ids) {
			idList.add(id);
		}

		QueryBuilder<T> builder = queryBuilderFactory.newBuilder(entityType);
		builder.addFilter(primaryKeyAttr.getName(), FilterOperator.EQUAL, ids);
		processor.prepareQuery(builder, queryParams);

		QueryExecutor<T> executor = builder.buildExecutor();
		processor.prepareExecution(executor, queryParams);

		List<T> list = executor.getResultList();
		return list;
	}

	@Override
	public <S extends T> S save(S entity) {
		EntityManager em = module.getEntityManager();
		em.persist(entity);
		return entity;
	}

	@Override
	public void delete(ID id) {
		EntityManager em = module.getEntityManager();

		T object = em.find(entityType, id);
		if (object != null) {
			em.remove(object);
		}
	}

	public Class<T> getEntityType() {
		return entityType;
	}
}
