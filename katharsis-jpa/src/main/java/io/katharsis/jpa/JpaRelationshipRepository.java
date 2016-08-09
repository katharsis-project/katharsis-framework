package io.katharsis.jpa;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.EntityManager;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.internal.meta.MetaType;
import io.katharsis.jpa.internal.query.QueryBuilder;
import io.katharsis.jpa.internal.query.QueryBuilderFactory;
import io.katharsis.jpa.internal.query.QueryExecutor;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.RelationshipRepository;

public class JpaRelationshipRepository<T, T_ID extends Serializable, D, D_ID extends Serializable>
		implements RelationshipRepository<T, T_ID, D, D_ID> {

	private JpaModule module;

	private Class<T> entityType;
	private MetaEntity entityMeta;

	public JpaRelationshipRepository(JpaModule module, Class<T> entityType) {
		this.entityType = entityType;
		this.entityMeta = MetaLookup.INSTANCE.getMeta(entityType).asEntity();
		this.module = module;
	}

	@Override
	public void setRelation(T source, D_ID targetId, String fieldName) {
		MetaAttribute attrMeta = entityMeta.getAttribute(fieldName);
		MetaAttribute oppositeAttrMeta = attrMeta.getOppositeAttribute();
		Class<?> targetType = getElementType(attrMeta);

		EntityManager em = module.getEntityManager();
		Object target = targetId != null ? em.find(targetType, targetId) : null;
		attrMeta.setValue(source, target);

		if (target != null && oppositeAttrMeta != null) {
			if (oppositeAttrMeta.getType().isCollection()) {
				oppositeAttrMeta.addValue(target, source);
			} else {
				oppositeAttrMeta.setValue(target, source);
			}
			em.persist(target);
		}
	}

	@Override
	public void setRelations(T source, Iterable<D_ID> targetIds, String fieldName) {
		// TODO batch read
		MetaAttribute attrMeta = entityMeta.getAttribute(fieldName);
		MetaAttribute oppositeAttrMeta = attrMeta.getOppositeAttribute();
		Class<?> targetType = getElementType(attrMeta);

		EntityManager em = module.getEntityManager();
		Collection<Object> targets = (Collection<Object>) attrMeta.getType().asCollection().newInstance();
		for (D_ID targetId : targetIds) {
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
				} else {
					oppositeAttrMeta.setValue(prevTarget, null);
				}
			}
		}

		// attach new targets
		for (Object target : targets) {
			if (oppositeAttrMeta != null) {
				if (oppositeAttrMeta.getType().isCollection()) {
					oppositeAttrMeta.addValue(target, source);
				} else {
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
	public void addRelations(T source, Iterable<D_ID> targetIds, String fieldName) {
		// TODO batch read
		MetaAttribute attrMeta = entityMeta.getAttribute(fieldName);
		MetaAttribute oppositeAttrMeta = attrMeta.getOppositeAttribute();
		Class<?> targetType = getElementType(attrMeta);

		EntityManager em = module.getEntityManager();
		for (D_ID targetId : targetIds) {
			Object target = em.find(targetType, targetId);
			attrMeta.addValue(source, target);

			if (oppositeAttrMeta != null) {
				if (oppositeAttrMeta.getType().isCollection()) {
					oppositeAttrMeta.addValue(target, source);
				} else {
					oppositeAttrMeta.setValue(target, source);
				}
				em.persist(target);
			}
		}
		em.persist(source);
	}

	@Override
	public void removeRelations(T source, Iterable<D_ID> targetIds, String fieldName) {
		// TODO batch read
		MetaAttribute attrMeta = entityMeta.getAttribute(fieldName);
		MetaAttribute oppositeAttrMeta = attrMeta.getOppositeAttribute();
		Class<?> targetType = getElementType(attrMeta);

		EntityManager em = module.getEntityManager();
		for (D_ID targetId : targetIds) {
			Object target = em.find(targetType, targetId);
			attrMeta.removeValue(source, target);

			if (target != null && oppositeAttrMeta != null) {
				if (oppositeAttrMeta.getType().isCollection()) {
					oppositeAttrMeta.removeValue(target, source);
				} else {
					oppositeAttrMeta.setValue(target, null);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public D findOneTarget(T_ID sourceId, String fieldName, QueryParams queryParams) {
		QueryExecutor<?> executor = getExecutor(sourceId, fieldName, queryParams);
		return (D) executor.getUniqueResult(true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<D> findManyTargets(T_ID sourceId, String fieldName, QueryParams queryParams) {
		QueryExecutor<?> executor = getExecutor(sourceId, fieldName, queryParams);
		return (Iterable<D>) executor.getResultList();
	}

	private QueryExecutor<?> getExecutor(T_ID sourceId, String fieldName, QueryParams queryParams) {
		QueryParamsProcessor processor = module.getProcessor();
		QueryBuilderFactory queryBuilderFactory = module.getQueryBuilderFactory();

		QueryBuilder<?> builder = queryBuilderFactory.newBuilder(entityType, fieldName, Arrays.asList(sourceId));
		processor.prepareQuery(builder, queryParams);
		QueryExecutor<?> executor = builder.buildExecutor();
		processor.prepareExecution(executor, queryParams);
		return executor;
	}
}
