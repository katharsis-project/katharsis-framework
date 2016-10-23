package io.katharsis.jpa;

import io.katharsis.jpa.internal.JpaRepositoryBase;
import io.katharsis.jpa.internal.JpaRepositoryUtils;
import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaType;
import io.katharsis.jpa.internal.paging.DefaultPagedMetaInformation;
import io.katharsis.jpa.internal.paging.PagedMetaInformation;
import io.katharsis.jpa.mapping.IdentityMapper;
import io.katharsis.jpa.mapping.JpaMapper;
import io.katharsis.jpa.mapping.JpaMapping;
import io.katharsis.jpa.query.*;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecBulkRelationshipRepository;
import io.katharsis.response.paging.PagedResultList;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.*;

public class JpaRelationshipRepository<S, I extends Serializable, T, J extends Serializable> extends JpaRepositoryBase<T>
		implements QuerySpecBulkRelationshipRepository<S, I, T, J> {

	private Class<S> sourceResourceClass;

	private Class<?> sourceEntityClass;

	private MetaEntity entityMeta;

	private JpaMapper<?, S> sourceMapper;

	/**
	 * JPA relationship directly exposed as repository
	 * 
	 * @param module
	 * @param sourceResourceClass
	 * @param targetResourceClass
	 */
	public JpaRelationshipRepository(JpaModule module, Class<S> sourceResourceClass, Class<T> targetResourceClass) {
		super(module, targetResourceClass);
		this.sourceResourceClass = sourceResourceClass;

		JpaMapping<?, S> sourceMapping = module.getMapping(sourceResourceClass);
		if (sourceMapping != null) {
			this.sourceEntityClass = sourceMapping.getEntityClass();
			this.sourceMapper = sourceMapping.getMapper();
		}
		else {
			this.sourceEntityClass = sourceResourceClass;
			this.sourceMapper = IdentityMapper.newInstance();
		}
		this.entityMeta = module.getMetaLookup().getMeta(sourceEntityClass).asEntity();
	}

	@Override
	public void setRelation(S source, J targetId, String fieldName) {
		checkUpdateable();
		MetaAttribute attrMeta = entityMeta.getAttribute(fieldName);
		MetaAttribute oppositeAttrMeta = attrMeta.getOppositeAttribute();
		Class<?> targetType = getElementType(attrMeta);

		Object sourceEntity = sourceMapper.unmap(source);

		EntityManager em = module.getEntityManager();
		Object target = targetId != null ? em.find(targetType, targetId) : null;
		attrMeta.setValue(sourceEntity, target);

		if (target != null && oppositeAttrMeta != null) {
			if (oppositeAttrMeta.getType().isCollection()) {
				oppositeAttrMeta.addValue(target, sourceEntity);
			}
			else {
				oppositeAttrMeta.setValue(target, sourceEntity);
			}
			em.persist(target);
		}
	}

	@Override
	public void setRelations(S source, Iterable<J> targetIds, String fieldName) {
		MetaAttribute attrMeta = entityMeta.getAttribute(fieldName);
		MetaAttribute oppositeAttrMeta = attrMeta.getOppositeAttribute();
		Class<?> targetType = getElementType(attrMeta);

		Object sourceEntity = sourceMapper.unmap(source);

		EntityManager em = module.getEntityManager();
		Collection<Object> targets = attrMeta.getType().asCollection().newInstance();
		for (J targetId : targetIds) {
			checkCreateable();

			Object target = em.find(targetType, targetId);
			targets.add(target);
		}

		// detach current
		if (oppositeAttrMeta != null) {
			Collection<?> col = (Collection<?>) attrMeta.getValue(sourceEntity);
			Iterator<?> iterator = col.iterator();
			while (iterator.hasNext()) {
				Object prevTarget = iterator.next();
				checkDeleteable();
				iterator.remove();
				if (oppositeAttrMeta.getType().isCollection()) {
					oppositeAttrMeta.removeValue(prevTarget, sourceEntity);
				}
				else {
					oppositeAttrMeta.setValue(prevTarget, null);
				}
			}
		}

		// attach new targets
		for (Object target : targets) {
			if (oppositeAttrMeta != null) {
				checkCreateable();
				if (oppositeAttrMeta.getType().isCollection()) {
					oppositeAttrMeta.addValue(target, sourceEntity);
				}
				else {
					oppositeAttrMeta.setValue(target, sourceEntity);
				}
				em.persist(target);
			}
		}
		attrMeta.setValue(sourceEntity, targets);
	}

	private Class<?> getElementType(MetaAttribute attrMeta) {
		MetaType type = attrMeta.getType();
		if (type.isCollection())
			return type.asCollection().getElementType().getImplementationClass();
		else
			return type.getImplementationClass();
	}

	@Override
	public void addRelations(S source, Iterable<J> targetIds, String fieldName) {
		checkCreateable();
		MetaAttribute attrMeta = entityMeta.getAttribute(fieldName);
		MetaAttribute oppositeAttrMeta = attrMeta.getOppositeAttribute();
		Class<?> targetType = getElementType(attrMeta);

		Object sourceEntity = sourceMapper.unmap(source);

		EntityManager em = module.getEntityManager();
		for (J targetId : targetIds) {
			Object target = em.find(targetType, targetId);
			attrMeta.addValue(sourceEntity, target);

			if (oppositeAttrMeta != null) {
				if (oppositeAttrMeta.getType().isCollection()) {
					oppositeAttrMeta.addValue(target, sourceEntity);
				}
				else {
					oppositeAttrMeta.setValue(target, sourceEntity);
				}
				em.persist(target);
			}
		}
		em.persist(sourceEntity);
	}

	@Override
	public void removeRelations(S source, Iterable<J> targetIds, String fieldName) {
		checkDeleteable();
		MetaAttribute attrMeta = entityMeta.getAttribute(fieldName);
		MetaAttribute oppositeAttrMeta = attrMeta.getOppositeAttribute();
		Class<?> targetType = getElementType(attrMeta);

		Object sourceEntity = sourceMapper.unmap(source);

		EntityManager em = module.getEntityManager();
		for (J targetId : targetIds) {
			Object target = em.find(targetType, targetId);
			attrMeta.removeValue(sourceEntity, target);

			if (target != null && oppositeAttrMeta != null) {
				if (oppositeAttrMeta.getType().isCollection()) {
					oppositeAttrMeta.removeValue(target, sourceEntity);
				}
				else {
					oppositeAttrMeta.setValue(target, null);
				}
			}
		}
	}

	@Override
	public Map<I, T> findOneTargets(Iterable<I> sourceIds, String fieldName, QuerySpec querySpec) {
		checkReadable();
		return getUniqueOrNull(getResults(sourceIds, fieldName, querySpec));
	}

	@Override
	public Map<I, Iterable<T>> findManyTargets(Iterable<I> sourceIds, String fieldName, QuerySpec querySpec) {
		checkReadable();
		return getResults(sourceIds, fieldName, querySpec);

	}

	private Map<I, Iterable<T>> getResults(Iterable<I> sourceIds, String fieldName, QuerySpec querySpec) {
		resetEntityManager();
		
		List<I> sourceIdLists = new ArrayList<>();
		for (I sourceId : sourceIds) {
			sourceIdLists.add(sourceId);
		}

		if (querySpec.getPagingSpec() != null && sourceIdLists.size() > 1) {
			throw new UnsupportedOperationException("page limit not supported for bulk inclusions");
		}

		QuerySpec bulkQuerySpec = querySpec.duplicate();
		
		QuerySpec filteredQuerySpec = filterQuerySpec(bulkQuerySpec);

		JpaQueryFactory queryFactory = module.getQueryFactory();
		JpaQuery<?> query = queryFactory.query(sourceEntityClass, fieldName, sourceIdLists);
		query.addParentIdSelection();
		query = filterQuery(filteredQuerySpec, query);

		ComputedAttributeRegistry computedAttributesRegistry = queryFactory.getComputedAttributes();
		Set<String> computedAttrs = computedAttributesRegistry.getForType(entityClass);

		JpaRepositoryUtils.prepareQuery(query, filteredQuerySpec, computedAttrs);

		JpaQueryExecutor<?> executor = query.buildExecutor();
		JpaRepositoryUtils.prepareExecutor(executor, filteredQuerySpec, fetchRelations(fieldName));
		executor = filterExecutor(filteredQuerySpec, executor);

		List<Tuple> tuples = executor.getResultTuples();
		
		tuples = filterTuples(bulkQuerySpec, tuples);

		Map<I, Iterable<T>> map = mapTuples(tuples);

		// support paging for non-bulk requests
		if (sourceIdLists.size() == 1 && querySpec.getPagingSpec() != null) {
			long totalRowCount = executor.getTotalRowCount();
			I sourceId = sourceIdLists.get(0);
			Iterable<T> iterable = map.get(sourceId);
			if (iterable == null) {
				iterable = new ArrayList<>();
			}
			map.put(sourceId, new PagedResultList<>((List<T>) iterable, totalRowCount));
		}

		resetEntityManager();
		return map;
	}

	@SuppressWarnings("unchecked")
	private Map<I, Iterable<T>> mapTuples(List<Tuple> tuples) {
		Map<I, Iterable<T>> map = new HashMap<>();
		for (Tuple tuple : tuples) {
			I sourceId = (I) tuple.get(0, Object.class);
			tuple.reduce(1);

			List<T> iterable = (List<T>) map.get(sourceId);
			if (iterable == null) {
				iterable = new ArrayList<>();
				map.put(sourceId, iterable);
			}
			iterable.add(mapper.map(tuple));
		}
		return map;
	}

	@Override
	public T findOneTarget(I sourceId, String fieldName, QuerySpec querySpec) {
		Map<I, T> map = findOneTargets(Arrays.asList(sourceId), fieldName, querySpec);
		if (map.isEmpty()) {
			return null;
		}
		else if (map.containsKey(sourceId) && map.size() == 1) {
			return map.get(sourceId);
		}
		else {
			throw new IllegalStateException("expected sourceId=" + sourceId + "in result " + map);
		}
	}

	@Override
	public List<T> findManyTargets(I sourceId, String fieldName, QuerySpec querySpec) {
		Map<I, Iterable<T>> map = findManyTargets(Arrays.asList(sourceId), fieldName, querySpec);
		if (map.isEmpty()) {
			return Collections.emptyList();
		}
		else if (map.containsKey(sourceId) && map.size() == 1) {
			return (List<T>) map.get(sourceId);
		}
		else {
			throw new IllegalStateException("expected sourceId=" + sourceId + "in result " + map);
		}
	}

	@Override
	public Class<S> getSourceResourceClass() {
		return sourceResourceClass;
	}

	@Override
	public Class<T> getTargetResourceClass() {
		return resourceClass;
	}

	public Class<?> getTargetEntityClass() {
		return entityClass;
	}

	@Override
	protected PagedMetaInformation newPagedMetaInformation() {
		return new DefaultPagedMetaInformation();
	}
}
