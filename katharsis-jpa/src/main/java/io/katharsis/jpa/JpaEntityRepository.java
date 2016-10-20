package io.katharsis.jpa;

import io.katharsis.jpa.internal.JpaRepositoryBase;
import io.katharsis.jpa.internal.JpaRepositoryUtils;
import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.paging.DefaultPagedMetaInformation;
import io.katharsis.jpa.internal.paging.PagedMetaInformation;
import io.katharsis.jpa.query.*;
import io.katharsis.queryspec.FilterOperator;
import io.katharsis.queryspec.FilterSpec;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecResourceRepository;
import io.katharsis.response.paging.PagedResultList;
import io.katharsis.utils.PropertyUtils;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Exposes a JPA entity as ResourceRepository.
 */
public class JpaEntityRepository<T, I extends Serializable> extends JpaRepositoryBase<T>
		implements QuerySpecResourceRepository<T, I> {

	private MetaEntity meta;

	private MetaAttribute primaryKeyAttr;

	public JpaEntityRepository(JpaModule module, Class<T> resourceType) {
		super(module, resourceType);
		this.meta = module.getMetaLookup().getMeta(entityClass).asEntity();
		this.primaryKeyAttr = JpaRepositoryUtils.getPrimaryKeyAttr(meta);
	}

	@Override
	public final T findOne(I id, QuerySpec querySpec) {
		checkReadable();
		QuerySpec idQuerySpec = querySpec.duplicate();
		idQuerySpec.addFilter(new FilterSpec(Arrays.asList(primaryKeyAttr.getName()), FilterOperator.EQ, id));
		List<T> results = findAll(idQuerySpec);
		return getUniqueOrNull(results);
	}

	@Override
	public final List<T> findAll(Iterable<I> ids, QuerySpec querySpec) {
		checkReadable();
		QuerySpec idQuerySpec = querySpec.duplicate();
		idQuerySpec.addFilter(new FilterSpec(Arrays.asList(primaryKeyAttr.getName()), FilterOperator.EQ, ids));
		return findAll(idQuerySpec);
	}

	@Override
	public List<T> findAll(QuerySpec querySpec) {
		resetEntityManager();
		checkReadable();
		QuerySpec filteredQuerySpec = filterQuerySpec(querySpec);
		JpaQueryFactory queryFactory = module.getQueryFactory();
		JpaQuery<?> query = queryFactory.query(entityClass);

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
		List<T> resources = map(tuples);
		resources = filterResults(filteredQuerySpec, resources);
		if (filteredQuerySpec.getPagingSpec() != null) {
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
		if(em.contains(entity)){
			checkUpdateable();
		}else{
			checkCreateable();
		}
		
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
		checkDeleteable();
		EntityManager em = module.getEntityManager();

		Object object = em.find(entityClass, id);
		if (object != null) {
			em.remove(object);
		}
	}

	@Override
	public Class<T> getResourceClass() {
		return resourceClass;
	}

	public Class<?> getEntityClass() {
		return entityClass;
	}

	@Override
	protected PagedMetaInformation newPagedMetaInformation() {
		return new DefaultPagedMetaInformation();
	}
}
