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
import io.katharsis.jpa.internal.paging.DefaultPagedLinksInformation;
import io.katharsis.jpa.internal.paging.DefaultPagedMetaInformation;
import io.katharsis.jpa.internal.paging.PagedLinksInformation;
import io.katharsis.jpa.internal.paging.PagedMetaInformation;
import io.katharsis.jpa.internal.paging.PagedResultList;
import io.katharsis.jpa.mapping.IdentityMapper;
import io.katharsis.jpa.mapping.JpaMapper;
import io.katharsis.jpa.query.ComputedAttributeRegistry;
import io.katharsis.jpa.query.JpaQuery;
import io.katharsis.jpa.query.JpaQueryExecutor;
import io.katharsis.jpa.query.JpaQueryFactory;
import io.katharsis.jpa.query.Tuple;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecRelationshipRepository;

public class JpaRelationshipRepository<S, I extends Serializable, T, J extends Serializable> extends JpaRepositoryBase<T>
		implements QuerySpecRelationshipRepository<S, I, T, J> {

	private Class<S> sourceEntityClass;

	private Class<?> targetEntityClass;

	private MetaEntity entityMeta;

	public JpaRelationshipRepository(JpaModule module, Class<S> entityClass, Class<T> relatedEntityClass) {
		super(module, relatedEntityClass, IdentityMapper.newInstance());
		this.sourceEntityClass = entityClass;
		this.entityMeta = module.getMetaLookup().getMeta(entityClass).asEntity();
		this.targetEntityClass = relatedEntityClass;
	}

	public <E> JpaRelationshipRepository(JpaModule module, Class<S> entityClass, Class<E> relatedEntityClass,
			Class<T> relatedDtoClass, JpaMapper<E, T> mapper) {
		super(module, relatedDtoClass, mapper);
		this.sourceEntityClass = entityClass;
		this.entityMeta = module.getMetaLookup().getMeta(entityClass).asEntity();
		this.targetEntityClass = relatedEntityClass;
	}

	@Override
	public void setRelation(S source, J targetId, String fieldName) {
		MetaAttribute attrMeta = entityMeta.getAttribute(fieldName);
		MetaAttribute oppositeAttrMeta = attrMeta.getOppositeAttribute();
		Class<?> targetType = getElementType(attrMeta);

		EntityManager em = module.getEntityManager();
		Object target = targetId != null ? em.find(targetType, targetId) : null;
		attrMeta.setValue(source, target);

		if (target != null && oppositeAttrMeta != null) {
			if (oppositeAttrMeta.getType().isCollection()) {
				oppositeAttrMeta.addValue(target, source);
			}
			else {
				oppositeAttrMeta.setValue(target, source);
			}
			em.persist(target);
		}
	}

	@Override
	public void setRelations(S source, Iterable<J> targetIds, String fieldName) {
		MetaAttribute attrMeta = entityMeta.getAttribute(fieldName);
		MetaAttribute oppositeAttrMeta = attrMeta.getOppositeAttribute();
		Class<?> targetType = getElementType(attrMeta);

		EntityManager em = module.getEntityManager();
		Collection<Object> targets = attrMeta.getType().asCollection().newInstance();
		for (J targetId : targetIds) {
			Object target = em.find(targetType, targetId);
			targets.add(target);
		}

		// detach current
		if (oppositeAttrMeta != null) {
			Collection<?> col = (Collection<?>) attrMeta.getValue(source);
			Iterator<?> iterator = col.iterator();
			while (iterator.hasNext()) {
				Object prevTarget = iterator.next();
				iterator.remove();
				if (oppositeAttrMeta.getType().isCollection()) {
					oppositeAttrMeta.removeValue(prevTarget, source);
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
					oppositeAttrMeta.addValue(target, source);
				}
				else {
					oppositeAttrMeta.setValue(target, source);
				}
				em.persist(target);
			}
		}
		attrMeta.setValue(source, targets);
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

		EntityManager em = module.getEntityManager();
		for (J targetId : targetIds) {
			Object target = em.find(targetType, targetId);
			attrMeta.addValue(source, target);

			if (oppositeAttrMeta != null) {
				if (oppositeAttrMeta.getType().isCollection()) {
					oppositeAttrMeta.addValue(target, source);
				}
				else {
					oppositeAttrMeta.setValue(target, source);
				}
				em.persist(target);
			}
		}
		em.persist(source);
	}

	@Override
	public void removeRelations(S source, Iterable<J> targetIds, String fieldName) {
		MetaAttribute attrMeta = entityMeta.getAttribute(fieldName);
		MetaAttribute oppositeAttrMeta = attrMeta.getOppositeAttribute();
		Class<?> targetType = getElementType(attrMeta);

		EntityManager em = module.getEntityManager();
		for (J targetId : targetIds) {
			Object target = em.find(targetType, targetId);
			attrMeta.removeValue(source, target);

			if (target != null && oppositeAttrMeta != null) {
				if (oppositeAttrMeta.getType().isCollection()) {
					oppositeAttrMeta.removeValue(target, source);
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
			list = new PagedResultList<>(list, totalRowCount, sourceEntityClass, sourceId, fieldName);
		}
		else {
			return list;
		}
		return filterResults(querySpec, list);
	}

	@Override
	public Class<S> getSourceResourceClass() {
		return sourceEntityClass;
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

	@Override
	protected PagedLinksInformation newPagedLinksInformation() {
		return new DefaultPagedLinksInformation();
	}
}
