package io.katharsis.jpa;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import io.katharsis.jpa.internal.JpaRepositoryBase;
import io.katharsis.jpa.internal.JpaRepositoryUtils;
import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.paging.DefaultPagedMetaInformation;
import io.katharsis.jpa.internal.paging.PagedMetaInformation;
import io.katharsis.jpa.mapping.IdentityMapper;
import io.katharsis.jpa.mapping.JpaMapper;
import io.katharsis.jpa.query.ComputedAttributeRegistry;
import io.katharsis.jpa.query.JpaQuery;
import io.katharsis.jpa.query.JpaQueryExecutor;
import io.katharsis.jpa.query.JpaQueryFactory;
import io.katharsis.jpa.query.Tuple;
import io.katharsis.queryspec.FilterOperator;
import io.katharsis.queryspec.FilterSpec;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecResourceRepository;
import io.katharsis.response.paging.PagedResultList;
import io.katharsis.utils.PropertyUtils;

/**
 * Exposes a JPA entity as ResourceRepository.
 */
public class JpaEntityRepository<T, I extends Serializable> extends JpaRepositoryBase<T>
		implements QuerySpecResourceRepository<T, I> {

	/**
	 * In case of a mapping the entityType differents from the resourceType
	 */
	private Class<?> entityType;

	private MetaEntity meta;

	private MetaAttribute primaryKeyAttr;

	public JpaEntityRepository(JpaModule module, Class<T> entityType) {
		super(module, entityType, IdentityMapper.newInstance());
		this.entityType = entityType;
		this.meta = module.getMetaLookup().getMeta(entityType).asEntity();
		this.primaryKeyAttr = JpaRepositoryUtils.getPrimaryKeyAttr(meta);
	}

	public <E> JpaEntityRepository(JpaModule module, Class<E> entityType, Class<T> resourceType, JpaMapper<E, T> mapper) {
		super(module, resourceType, mapper);
		this.entityType = entityType;
		this.meta = module.getMetaLookup().getMeta(entityType).asEntity();
		this.primaryKeyAttr = JpaRepositoryUtils.getPrimaryKeyAttr(meta);
	}

	@Override
	public final T findOne(I id, QuerySpec querySpec) {
		QuerySpec idQuerySpec = querySpec.duplicate();
		idQuerySpec.addFilter(new FilterSpec(Arrays.asList(primaryKeyAttr.getName()), FilterOperator.EQ, id));
		List<T> results = findAll(idQuerySpec);
		return getUniqueOrNull(results);
	}

	@Override
	public final List<T> findAll(Iterable<I> ids, QuerySpec querySpec) {
		QuerySpec idQuerySpec = querySpec.duplicate();
		idQuerySpec.addFilter(new FilterSpec(Arrays.asList(primaryKeyAttr.getName()), FilterOperator.EQ, ids));
		return findAll(querySpec);
	}

	@Override
	public List<T> findAll(QuerySpec querySpec) {
		QuerySpec filteredQuerySpec = filterQuerySpec(querySpec);
		JpaQueryFactory queryFactory = module.getQueryFactory();
		JpaQuery<?> query = queryFactory.query(entityType);

		ComputedAttributeRegistry computedAttributesRegistry = queryFactory.getComputedAttributes();
		Set<String> computedAttrs = computedAttributesRegistry.getForType(entityType);

		JpaRepositoryUtils.prepareQuery(query, filteredQuerySpec, computedAttrs);
		query = filterQuery(filteredQuerySpec, query);
		JpaQueryExecutor<?> executor = query.buildExecutor();
		JpaRepositoryUtils.prepareExecutor(executor, filteredQuerySpec);
		executor = filterExecutor(filteredQuerySpec, executor);

		List<Tuple> tuples = executor.getResultTuples();
		tuples = filterTuples(filteredQuerySpec, tuples);
		List<T> resources = map(tuples);
		resources = filterResults(filteredQuerySpec, resources);
		if (filteredQuerySpec.getLimit() != null) {
			long totalRowCount = executor.getTotalRowCount();
			return new PagedResultList<>(resources, totalRowCount);
		}
		else {
			return resources;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <S extends T> S save(S resource) {
		Object entity = mapper.unmap(resource);

		EntityManager em = module.getEntityManager();
		em.persist(entity);
		em.flush();

		// fetch again since we may have to fetch tuple data and do DTO mapping
		QuerySpec querySpec = new QuerySpec(resourceClass);
		I id = (I) PropertyUtils.getProperty(resource, primaryKeyAttr.getName());
		if (id == null) {
			throw new IllegalStateException("id not available for entity " + id);
		}
		return (S) findOne(id, querySpec);
	}

	@Override
	public void delete(I id) {
		EntityManager em = module.getEntityManager();

		Object object = em.find(entityType, id);
		if (object != null) {
			em.remove(object);
		}
	}

	@Override
	public Class<T> getResourceClass() {
		return resourceClass;
	}

	public Class<?> getEntityClass() {
		return entityType;
	}

	@Override
	protected PagedMetaInformation newPagedMetaInformation() {
		return new DefaultPagedMetaInformation();
	}
}
