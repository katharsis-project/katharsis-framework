package io.katharsis.jpa;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.persistence.EntityManager;

import io.katharsis.jpa.internal.JpaRepositoryUtils;
import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaType;
import io.katharsis.jpa.query.JpaQuery;
import io.katharsis.jpa.query.JpaQueryExecutor;
import io.katharsis.jpa.query.JpaQueryFactory;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecRelationshipRepository;

public class JpaRelationshipRepository<T, I extends Serializable, D, J extends Serializable>
		implements QuerySpecRelationshipRepository<T, I, D, J> {

	private JpaModule module;

	private Class<T> entityClass;

	private Class<D> relatedEntityClass;

	private MetaEntity entityMeta;

	public JpaRelationshipRepository(JpaModule module, Class<T> entityClass, Class<D> relatedEntityClass) {
		this.module = module;
		this.entityClass = entityClass;
		this.entityMeta = module.getMetaLookup().getMeta(entityClass).asEntity();
		this.relatedEntityClass = relatedEntityClass;
	}

	@Override
	public void setRelation(T source, J targetId, String fieldName) {
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
	public void setRelations(T source, Iterable<J> targetIds, String fieldName) {
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
	public void addRelations(T source, Iterable<J> targetIds, String fieldName) {
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
	public void removeRelations(T source, Iterable<J> targetIds, String fieldName) {
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

	@SuppressWarnings("unchecked")
	@Override
	public D findOneTarget(I sourceId, String fieldName, QuerySpec querySpec) {
		JpaQueryExecutor<?> executor = getExecutor(sourceId, fieldName, querySpec);
		return (D) executor.getUniqueResult(true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<D> findManyTargets(I sourceId, String fieldName, QuerySpec querySpec) {
		JpaQueryExecutor<?> executor = getExecutor(sourceId, fieldName, querySpec);
		return (Iterable<D>) executor.getResultList();
	}

	private JpaQueryExecutor<?> getExecutor(I sourceId, String fieldName, QuerySpec querySpec) {
		JpaQueryFactory queryBuilderFactory = module.getQueryFactory();
		JpaQuery<?> query = queryBuilderFactory.query(entityClass, fieldName, Arrays.asList(sourceId));
		JpaRepositoryUtils.prepareQuery(query, querySpec);
		JpaQueryExecutor<?> executor = query.buildExecutor();
		JpaRepositoryUtils.prepareExecutor(executor, querySpec);
		return executor;
	}

	@Override
	public Class<T> getSourceResourceClass() {
		return entityClass;
	}

	@Override
	public Class<D> getTargetResourceClass() {
		return relatedEntityClass;
	}
}
