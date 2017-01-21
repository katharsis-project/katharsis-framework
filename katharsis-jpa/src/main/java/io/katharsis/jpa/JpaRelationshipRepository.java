package io.katharsis.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import io.katharsis.core.internal.utils.MultivaluedMap;
import io.katharsis.jpa.internal.JpaRepositoryBase;
import io.katharsis.jpa.internal.JpaRepositoryUtils;
import io.katharsis.jpa.internal.JpaRequestContext;
import io.katharsis.jpa.mapping.IdentityMapper;
import io.katharsis.jpa.mapping.JpaMapper;
import io.katharsis.jpa.meta.MetaEntity;
import io.katharsis.jpa.query.ComputedAttributeRegistry;
import io.katharsis.jpa.query.JpaQuery;
import io.katharsis.jpa.query.JpaQueryExecutor;
import io.katharsis.jpa.query.JpaQueryFactory;
import io.katharsis.jpa.query.Tuple;
import io.katharsis.meta.model.MetaAttribute;
import io.katharsis.meta.model.MetaType;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.BulkRelationshipRepositoryV2;
import io.katharsis.repository.RelationshipRepositoryV2;
import io.katharsis.resource.list.DefaultResourceList;
import io.katharsis.resource.list.ResourceList;
import io.katharsis.resource.meta.MetaInformation;
import io.katharsis.resource.meta.PagedMetaInformation;

public class JpaRelationshipRepository<S, I extends Serializable, T, J extends Serializable> extends JpaRepositoryBase<T> implements RelationshipRepositoryV2<S, I, T, J>, BulkRelationshipRepositoryV2<S, I, T, J> {

	private Class<S> sourceResourceClass;

	private Class<?> sourceEntityClass;

	private MetaEntity entityMeta;

	private JpaMapper<?, S> sourceMapper;

	/**
	 * JPA relationship directly exposed as repository
	 * 
	 * @param module
	 *            that manages this repository
	 * @param sourceResourceClass
	 *            from this relation
	 * @param targetResourceClass
	 *            from this relation
	 */
	public JpaRelationshipRepository(JpaModule module, Class<S> sourceResourceClass, JpaRepositoryConfig<T> targetResourceClass) {
		super(module, targetResourceClass);
		this.sourceResourceClass = sourceResourceClass;

		JpaRepositoryConfig<S> sourceMapping = module.getRepositoryConfig(sourceResourceClass);
		if (sourceMapping != null) {
			this.sourceEntityClass = sourceMapping.getEntityClass();
			this.sourceMapper = sourceMapping.getMapper();
		} else {
			this.sourceEntityClass = sourceResourceClass;
			this.sourceMapper = IdentityMapper.newInstance();
		}
		this.entityMeta = module.getJpaMetaLookup().getMeta(sourceEntityClass, MetaEntity.class);
	}

	@Override
	public void setRelation(S source, J targetId, String fieldName) {
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
			} else {
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

			Object target = em.find(targetType, targetId);
			targets.add(target);
		}

		// detach current
		if (oppositeAttrMeta != null) {
			Collection<?> col = (Collection<?>) attrMeta.getValue(sourceEntity);
			Iterator<?> iterator = col.iterator();
			while (iterator.hasNext()) {
				Object prevTarget = iterator.next();
				iterator.remove();
				if (oppositeAttrMeta.getType().isCollection()) {
					oppositeAttrMeta.removeValue(prevTarget, sourceEntity);
				} else {
					oppositeAttrMeta.setValue(prevTarget, null);
				}
			}
		}

		// attach new targets
		for (Object target : targets) {
			if (oppositeAttrMeta != null) {
				if (oppositeAttrMeta.getType().isCollection()) {
					oppositeAttrMeta.addValue(target, sourceEntity);
				} else {
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
				} else {
					oppositeAttrMeta.setValue(target, sourceEntity);
				}
				em.persist(target);
			}
		}
		em.persist(sourceEntity);
	}

	@Override
	public void removeRelations(S source, Iterable<J> targetIds, String fieldName) {
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
				} else {
					oppositeAttrMeta.setValue(target, null);
				}
			}
		}
	}

	@Override
	public MultivaluedMap<I, T> findTargets(Iterable<I> sourceIds, String fieldName, QuerySpec querySpec) {
		List<I> sourceIdLists = new ArrayList<>();
		for (I sourceId : sourceIds) {
			sourceIdLists.add(sourceId);
		}

		if (querySpec.getLimit() != null && sourceIdLists.size() > 1) {
			throw new UnsupportedOperationException("page limit not supported for bulk inclusions");
		}

		QuerySpec bulkQuerySpec = querySpec.duplicate();

		QuerySpec filteredQuerySpec = filterQuerySpec(bulkQuerySpec);

		JpaQueryFactory queryFactory = module.getQueryFactory();
		JpaQuery<?> query = queryFactory.query(sourceEntityClass, fieldName, sourceIdLists);
		query.setPrivateData(new JpaRequestContext(this, querySpec));
		query.addParentIdSelection();
		query = filterQuery(filteredQuerySpec, query);

		Class<?> entityClass = repositoryConfig.getEntityClass();
		ComputedAttributeRegistry computedAttributesRegistry = queryFactory.getComputedAttributes();
		Set<String> computedAttrs = computedAttributesRegistry.getForType(entityClass);

		JpaRepositoryUtils.prepareQuery(query, filteredQuerySpec, computedAttrs);

		JpaQueryExecutor<?> executor = query.buildExecutor();
		JpaRepositoryUtils.prepareExecutor(executor, filteredQuerySpec, fetchRelations(fieldName));
		executor = filterExecutor(filteredQuerySpec, executor);

		List<Tuple> tuples = executor.getResultTuples();

		tuples = filterTuples(bulkQuerySpec, tuples);

		MultivaluedMap<I, T> map = mapTuples(tuples);

		// support paging for non-bulk requests
		if (sourceIdLists.size() == 1 && querySpec.getLimit() != null) {
			I sourceId = sourceIdLists.get(0);
			ResourceList<T> iterable = (ResourceList<T>) map.getList(sourceId);

			MetaInformation metaInfo = iterable.getMeta();
			if (metaInfo instanceof PagedMetaInformation) {
				long totalRowCount = executor.getTotalRowCount();
				((PagedMetaInformation) metaInfo).setTotalResourceCount(totalRowCount);
			}
		}

		return map;
	}

	@SuppressWarnings("unchecked")
	private MultivaluedMap<I, T> mapTuples(List<Tuple> tuples) {
		MultivaluedMap<I, T> map = new MultivaluedMap<I, T>() {

			@Override
			protected List<T> newList() {
				return repositoryConfig.newResultList();
			}
		};
		for (Tuple tuple : tuples) {
			I sourceId = (I) tuple.get(0, Object.class);
			tuple.reduce(1);
			JpaMapper<Object, T> mapper = repositoryConfig.getMapper();
			map.add(sourceId, mapper.map(tuple));
		}
		return map;
	}

	@Override
	public T findOneTarget(I sourceId, String fieldName, QuerySpec querySpec) {
		MultivaluedMap<I, T> map = findTargets(Arrays.asList(sourceId), fieldName, querySpec);
		if (map.isEmpty()) {
			return null;
		} else {
			return map.getUnique(sourceId);
		}
	}

	@Override
	public ResourceList<T> findManyTargets(I sourceId, String fieldName, QuerySpec querySpec) {
		MultivaluedMap<I, T> map = findTargets(Arrays.asList(sourceId), fieldName, querySpec);
		if (map.isEmpty()) {
			return new DefaultResourceList<>();
		} else {
			return (ResourceList<T>) map.getList(sourceId);
		}
	}

	@Override
	public Class<S> getSourceResourceClass() {
		return sourceResourceClass;
	}

	@Override
	public Class<T> getTargetResourceClass() {
		return repositoryConfig.getResourceClass();
	}

	public Class<?> getTargetEntityClass() {
		return repositoryConfig.getEntityClass();
	}
}
