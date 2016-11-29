package io.katharsis.jpa;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import io.katharsis.jpa.internal.JpaRepositoryBase;
import io.katharsis.jpa.internal.JpaRepositoryUtils;
import io.katharsis.jpa.internal.JpaRequestContext;
import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.mapping.JpaMapper;
import io.katharsis.jpa.query.ComputedAttributeRegistry;
import io.katharsis.jpa.query.JpaQuery;
import io.katharsis.jpa.query.JpaQueryExecutor;
import io.katharsis.jpa.query.JpaQueryFactory;
import io.katharsis.jpa.query.Tuple;
import io.katharsis.queryspec.FilterOperator;
import io.katharsis.queryspec.FilterSpec;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepositoryV2;
import io.katharsis.resource.list.ResourceList;
import io.katharsis.response.MetaInformation;
import io.katharsis.response.paging.PagedMetaInformation;
import io.katharsis.utils.PropertyUtils;

/**
 * Exposes a JPA entity as ResourceRepository.
 */
public class JpaEntityRepository<T, I extends Serializable> extends JpaRepositoryBase<T>
		implements ResourceRepositoryV2<T, I> {

	private MetaEntity meta;

	private MetaAttribute primaryKeyAttr;

	public JpaEntityRepository(JpaModule module, JpaRepositoryConfig<T> config) {
		super(module, config);
		this.meta = module.getMetaLookup().getMeta(config.getEntityClass()).asEntity();
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
	public final ResourceList<T> findAll(Iterable<I> ids, QuerySpec querySpec) {
		QuerySpec idQuerySpec = querySpec.duplicate();
		idQuerySpec.addFilter(new FilterSpec(Arrays.asList(primaryKeyAttr.getName()), FilterOperator.EQ, ids));
		return findAll(idQuerySpec);
	}

	@Override
	public ResourceList<T> findAll(QuerySpec querySpec) {
		resetEntityManager();
		Class<?> entityClass = repositoryConfig.getEntityClass();
		QuerySpec filteredQuerySpec = filterQuerySpec(querySpec);
		JpaQueryFactory queryFactory = module.getQueryFactory();
		JpaQuery<?> query = queryFactory.query(entityClass);
		query.setPrivateData(new JpaRequestContext(this, querySpec));

		ComputedAttributeRegistry computedAttributesRegistry = queryFactory.getComputedAttributes();
		Set<String> computedAttrs = computedAttributesRegistry.getForType(entityClass);

		JpaRepositoryUtils.prepareQuery(query, filteredQuerySpec, computedAttrs);
		query = filterQuery(filteredQuerySpec, query);
		JpaQueryExecutor<?> executor = query.buildExecutor();
		JpaRepositoryUtils.prepareExecutor(executor, filteredQuerySpec, fetchRelations(null));
		executor = filterExecutor(filteredQuerySpec, executor);
		resetEntityManager();

		List<Tuple> tuples = executor.getResultTuples();
		tuples = filterTuples(filteredQuerySpec, tuples);
		ResourceList<T> resources = map(tuples);
		resources = filterResults(filteredQuerySpec, resources);

		if (filteredQuerySpec.getLimit() != null) {
			MetaInformation metaInfo = resources.getMeta();
			if (metaInfo instanceof PagedMetaInformation) {
				long totalRowCount = executor.getTotalRowCount();
				((PagedMetaInformation) metaInfo).setTotalResourceCount(totalRowCount);
			}
		}
		
		return resources;
	}
	
	@Override
	public <S extends T> S create(S resource) {
		return save(resource);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <S extends T> S save(S resource) {
		JpaMapper<Object, T> mapper = repositoryConfig.getMapper();
		Object entity = mapper.unmap(resource);

		EntityManager em = module.getEntityManager();
		em.persist(entity);
		em.flush();

		// fetch again since we may have to fetch tuple data and do DTO mapping
		QuerySpec querySpec = new QuerySpec(repositoryConfig.getResourceClass());
		I id = (I) PropertyUtils.getProperty(resource, primaryKeyAttr.getName());
		if (id == null) {
			throw new IllegalStateException("id not available for entity " + id);
		}
		return (S) findOne(id, querySpec);
	}

	@Override
	public void delete(I id) {
		EntityManager em = module.getEntityManager();

		Object object = em.find(repositoryConfig.getEntityClass(), id);
		if (object != null) {
			em.remove(object);
		}
	}

	@Override
	public Class<T> getResourceClass() {
		return repositoryConfig.getResourceClass();
	}

	public Class<?> getEntityClass() {
		return repositoryConfig.getEntityClass();
	}

}
