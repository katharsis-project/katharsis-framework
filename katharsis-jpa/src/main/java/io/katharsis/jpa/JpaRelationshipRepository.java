package io.katharsis.jpa;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import io.katharsis.jpa.internal.JpaRepositoryBase;
import io.katharsis.jpa.internal.JpaRepositoryUtils;
import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaType;
import io.katharsis.jpa.internal.paging.DefaultPagedMetaInformation;
import io.katharsis.jpa.internal.paging.PagedMetaInformation;
import io.katharsis.jpa.mapping.IdentityMapper;
import io.katharsis.jpa.mapping.JpaMapper;
import io.katharsis.jpa.query.ComputedAttributeRegistry;
import io.katharsis.jpa.query.JpaQuery;
import io.katharsis.jpa.query.JpaQueryExecutor;
import io.katharsis.jpa.query.JpaQueryFactory;
import io.katharsis.jpa.query.Tuple;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecRelationshipRepository;
import io.katharsis.response.paging.PagedResultList;

public class JpaRelationshipRepository<S, I extends Serializable, T, J extends Serializable> extends JpaRepositoryBase<T>
		implements QuerySpecRelationshipRepository<S, I, T, J> {

	private Class<S> sourceResourceClass;

	private Class<?> sourceEntityClass;

	private Class<?> targetEntityClass;

	private MetaEntity entityMeta;

	private JpaMapper<?, S> sourceMapper;

	/**
	 * JPA relationship directly exposed as repository
	 * 
	 * @param module
	 * @param sourceEntityClass
	 * @param relatedEntityClass
	 */
	public JpaRelationshipRepository(JpaModule module, Class<S> sourceEntityClass, Class<T> relatedEntityClass) {
		super(module, relatedEntityClass, IdentityMapper.newInstance());
		this.sourceEntityClass = sourceEntityClass;
		this.sourceResourceClass = sourceEntityClass;
		this.entityMeta = module.getMetaLookup().getMeta(sourceEntityClass).asEntity();
		this.targetEntityClass = relatedEntityClass;
		this.sourceMapper = IdentityMapper.newInstance();
	}

	/**
	 * JPA relationship mapped to a DTO relationship and exposed as repository
	 * 
	 * @param module
	 * @param sourceEntityClass
	 * @param sourceResourceClass
	 * @param relatedEntityClass
	 * @param relatedResourceClass
	 * @param sourceMapper
	 * @param targetMapper
	 */
	public <D, E> JpaRelationshipRepository(JpaModule module, Class<D> sourceEntityClass, Class<S> sourceResourceClass,
			Class<E> relatedEntityClass, Class<T> relatedResourceClass, JpaMapper<D, S> sourceMapper,
			JpaMapper<E, T> targetMapper) {
		super(module, relatedResourceClass, targetMapper);
		this.sourceResourceClass = sourceResourceClass;
		this.sourceEntityClass = sourceEntityClass;
		this.entityMeta = module.getMetaLookup().getMeta(sourceEntityClass).asEntity();
		this.targetEntityClass = relatedEntityClass;
		this.sourceMapper = sourceMapper;
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
				}
				else {
					oppositeAttrMeta.setValue(prevTarget, null);
				}
			}
		}

		// attach new targets
		for (Object target : targets) {
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
	public T findOneTarget(I sourceId, String fieldName, QuerySpec querySpec) {
		return getUniqueOrNull(getResults(sourceId, fieldName, querySpec));
	}

	@Override
	public List<T> findManyTargets(I sourceId, String fieldName, QuerySpec querySpec) {
		return getResults(sourceId, fieldName, querySpec);

	}

	private List<T> getResults(I sourceId, String fieldName, QuerySpec querySpec) {
		QuerySpec filteredQuerySpec = filterQuerySpec(querySpec);

		JpaQueryFactory queryFactory = module.getQueryFactory();
		JpaQuery<?> query = queryFactory.query(sourceEntityClass, fieldName, Arrays.asList(sourceId));
		query = filterQuery(filteredQuerySpec, query);

		ComputedAttributeRegistry computedAttributesRegistry = queryFactory.getComputedAttributes();
		Set<String> computedAttrs = computedAttributesRegistry.getForType(targetEntityClass);

		JpaRepositoryUtils.prepareQuery(query, filteredQuerySpec, computedAttrs);

		JpaQueryExecutor<?> executor = query.buildExecutor();
		JpaRepositoryUtils.prepareExecutor(executor, filteredQuerySpec);
		executor = filterExecutor(filteredQuerySpec, executor);

		List<Tuple> tuples = executor.getResultTuples();
		tuples = filterTuples(querySpec, tuples);

		List<T> list = map(tuples);

		// compute total row count if necessary to do proper paging
		if (querySpec.getLimit() != null) {
			long totalRowCount = executor.getTotalRowCount();
			list = new PagedResultList<>(list, totalRowCount);
		}
		else {
			return list;
		}
		return filterResults(querySpec, list);
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
		return targetEntityClass;
	}

	@Override
	protected PagedMetaInformation newPagedMetaInformation() {
		return new DefaultPagedMetaInformation();
	}
}
