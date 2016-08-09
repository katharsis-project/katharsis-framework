package io.katharsis.jpa.internal.query.impl;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaAttributePath;
import io.katharsis.jpa.internal.meta.MetaAttributeProjection;
import io.katharsis.jpa.internal.meta.MetaMapAttribute;
import io.katharsis.jpa.internal.query.impl.QueryBuilderImpl.QueryBuilderContext;

public class CriteriaJoinRegistry {

	private Map<MetaAttributePath, From<?, ?>> joinMap = new HashMap<MetaAttributePath, From<?, ?>>();

	private QueryBuilderContext<?> ctx;
	private QueryBuilderImpl<?> builder;

	public CriteriaJoinRegistry(QueryBuilderContext<?> ctx, QueryBuilderImpl<?> builder) {
		this.ctx = ctx;
		this.builder = builder;
	}

	public Expression<?> getEntityAttribute(MetaAttributePath attrPath) {
		MetaAttributePath associationPath = extractAssociationPath(attrPath);
		MetaAttributePath primitivePath = attrPath.subPath(associationPath.length());

		From<?, ?> from = getOrCreateJoin(associationPath);
		if (primitivePath.length() == 0) {
			return from;
		}

		MetaAttributePath currentPath = associationPath;
		Path<Object> criteriaPath = null;
		for (MetaAttribute pathElement : primitivePath) {
			currentPath = currentPath.concat(pathElement);

			Path<?> currentCriteriaPath = criteriaPath != null ? criteriaPath : from;
			if (pathElement instanceof MetaMapAttribute) {
				MetaMapAttribute mapPathElement = (MetaMapAttribute) pathElement;
				if (criteriaPath != null)
					throw new IllegalStateException("Cannot join to map");
				MapJoin<Object, Object, Object> mapJoin = ((From) currentCriteriaPath).joinMap(mapPathElement.getName(), JoinType.LEFT);
				if (mapPathElement.isKeyAccess()) {
					criteriaPath = mapJoin.key();
				} else if (mapPathElement.getKey() == null) {
					criteriaPath = mapJoin.value();
				} else {
					criteriaPath = mapJoin.value();
					Predicate mapJoinCondition = ctx.cb.equal(mapJoin.key(), mapPathElement.getKey());
					JoinType joinType = builder.getJoinType(currentPath);
					if (joinType == JoinType.LEFT) {
						Predicate nullCondition = ctx.cb.isNull(mapJoin.key());
						ctx.addPredicate(ctx.cb.or(mapJoinCondition, nullCondition));
					} else {
						ctx.addPredicate(mapJoinCondition);
					}
				}
			} else {
				// we may need to downcast if attribute is defined on a subtype
				Class<?> entityType = pathElement.getParent().asDataObject().getImplementationClass();
				boolean isSubType = !entityType.isAssignableFrom(currentCriteriaPath.getJavaType());
				if (isSubType) {
					currentCriteriaPath = ctx.cb.treat((Path) currentCriteriaPath, entityType);
				}
				criteriaPath = currentCriteriaPath.get(pathElement.getName());
			}
		}
		return criteriaPath;

	}

	protected static MetaAttributePath extractAssociationPath(MetaAttributePath path) {
		for (int i = path.length() - 1; i >= 0; i--) {
			MetaAttribute element = path.getElement(i);
			if (element.isAssociation()) {
				return path.subPath(0, i + 1);
			}
		}
		return new MetaAttributePath();
	}

	public From<?, ?> getOrCreateJoin(MetaAttributePath path) {
		if (path.length() == 0)
			return ctx.root;

		MetaAttributePath subPath = new MetaAttributePath();
		From<?, ?> from = ctx.root;

		for (int i = 0; i < path.length(); i++) {
			MetaAttribute pathElement = path.getElement(i);
			from = getOrCreateJoin(subPath, pathElement);
			subPath = subPath.concat(pathElement);
		}
		return from;
	}

	private From<?, ?> getOrCreateJoin(MetaAttributePath srcPath, MetaAttribute targetAttr) {
		MetaAttributePath path = srcPath.concat(targetAttr);
		From<?, ?> parent = joinMap.get(srcPath);
		From<?, ?> join = joinMap.get(path);
		if (join == null) {
			JoinType joinType = builder.getJoinType(path);
			if (targetAttr instanceof MetaAttributeProjection) {
				MetaAttributeProjection projAttr = (MetaAttributeProjection) targetAttr;
				join = builder.getVirtualAttrs().join((IQueryBuilderContext) ctx, parent, projAttr);
				// } else if (builder.getFetchJoins().contains(path)) {
				// join = (From<?, ?>) parent.fetch(targetAttr.getName(), joinType);
			} else {
				join = parent.join(targetAttr.getName(), joinType);
			}
			joinMap.put(path, join);
		}
		return join;
	}

	public void putJoin(MetaAttributePath path, From<?, ?> root) {
		if (joinMap.containsKey(path))
			throw new IllegalArgumentException(path.toString() + " already exists");
		joinMap.put(path, root);
	}

	//
	// /**
	// * Add a left fetch. This is used during 2nd pass of graphcontrol application to ensure all fetches are done.
	// */
	// public From<?, ?> addFetch(MetaAttributePath path) {
	// if (path.length() >= 2) {
	// // ensure parent is fetched
	// MetaAttributePath parentPath = path.subPath(0, path.length() - 1);
	// addFetch(parentPath);
	// }
	//
	// From<?, ?> join = getJoinInternal(root, srcAttrPath, joinAttr.getName());
	// if (join == null) {
	// join = (From<?, ?>) parent.fetch(joinAttr.getName(), JoinType.LEFT);
	// putJoinInternal(root, srcAttrPath, joinAttr.getName(), join);
	// }
	// return join;
	// }

}
