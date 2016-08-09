package io.katharsis.jpa.internal.query.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.SingularAttribute;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaAttributePath;
import io.katharsis.jpa.internal.meta.MetaAttributeProjection;
import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.meta.MetaKey;
import io.katharsis.jpa.internal.meta.MetaProjection;
import io.katharsis.jpa.internal.query.OrderSpec;

class QueryUtil {

	private static String toEntityPath(MetaProjection metaProjection, String attributePath) {
		MetaAttributePath derivationPath = metaProjection.resolvePath(attributePath);

		StringBuilder entityPath = new StringBuilder();
		for (MetaAttribute attr : derivationPath) {
			MetaAttributeProjection projAttr = (MetaAttributeProjection) attr;
			if (entityPath.length() > 0)
				entityPath.append(".");
			MetaAttributePath path = projAttr.getPath();
			entityPath.append(path.toString());
		}
		return entityPath.toString();
	}

	public static OrderSpec toEntitySpec(MetaDataObject metaObject, OrderSpec orderSpec) {
		if (metaObject instanceof MetaProjection) {
			MetaProjection metaProjection = (MetaProjection) metaObject;
			return new OrderSpec(toEntityPath(metaProjection, orderSpec.getPath()), orderSpec.getDirection());
		} else {
			return orderSpec;
		}
	}

	private static List<OrderSpec> toEntitySpec(MetaDataObject meta, List<OrderSpec> orderSpecs) {
		if (meta instanceof MetaProjection) {
			List<OrderSpec> entityOrderSpec = new ArrayList<OrderSpec>();
			for (OrderSpec orderSpec : orderSpecs) {
				entityOrderSpec.add(toEntitySpec(meta.asProjection(), orderSpec));
			}
			return entityOrderSpec;
		} else {
			return orderSpecs;
		}
	}

	public static boolean hasTotalOrder(MetaDataObject meta, List<OrderSpec> orderSpecs) {
		if (meta instanceof MetaProjection) {
			MetaProjection projectionMeta = (MetaProjection) meta;
			List<OrderSpec> entityOrderSpecs = toEntitySpec(meta, orderSpecs);
			return hasTotalOrder(projectionMeta.getBaseType(), entityOrderSpecs);
		} else {
			boolean hasTotalOrder = contains(meta.getPrimaryKey(), orderSpecs);
			if (hasTotalOrder)
				return true;
			// FIXME todo
//			for (MetaKey key : meta.getKeys()) {
//				if (key.isUnique() && contains(key, orderSpecs)) {
//					return true;
//				}
//			}
			return false;
		}
	}

	public static boolean contains(MetaKey key, List<OrderSpec> entityOrderSpecs) {
		for (MetaAttribute attr : key.getElements()) {
			boolean contains = false;
			for (OrderSpec orderSpec : entityOrderSpecs) {
				if (orderSpec.getPath().equals(attr.getName())) {
					contains = true;
					break;
				}
			}
			if (!contains)
				return false;
		}
		return true;
	}

	protected static boolean hasManyRootsFetchesOrJoins(CriteriaQuery<?> criteriaQuery) {
		Set<Root<?>> roots = criteriaQuery.getRoots();

		// more than one root, user is supposed to handle this manually
		if (roots.size() != 1)
			return false;

		for (Root<?> root : roots) {
			if (containsMultiRelationFetch(root.getFetches()))
				return true;

			if (containsMultiRelationJoin(root.getJoins()))
				return true;
		}
		return false;
	}

	private static boolean containsMultiRelationFetch(Set<?> fetches) {
		for (Object fetchObj : fetches) {
			Fetch<?, ?> fetch = (Fetch<?, ?>) fetchObj;

			Attribute<?, ?> attr = fetch.getAttribute();
			if (attr.isAssociation() && attr.isCollection())
				return true;

			if (containsMultiRelationFetch(fetch.getFetches()))
				return true;
		}
		return false;
	}

	private static boolean containsMultiRelationJoin(Set<?> fetches) {
		for (Object fetchObj : fetches) {
			Fetch<?, ?> fetch = (Fetch<?, ?>) fetchObj;
			Attribute<?, ?> attr = fetch.getAttribute();
			if (attr.isAssociation() && attr.isCollection())
				return true;

			if (containsMultiRelationFetch(fetch.getFetches()))
				return true;
		}
		return false;
	}

	protected static boolean containsRelation(Object expr) {
		if (expr instanceof Join) {
			return true;
		} else if (expr instanceof SingularAttribute) {
			SingularAttribute<?, ?> attr = (SingularAttribute<?, ?>) expr;
			return attr.isAssociation();
		} else if (expr instanceof Path) {
			Path<?> attrPath = (Path<?>) expr;
			Bindable<?> model = attrPath.getModel();
			Path<?> parent = attrPath.getParentPath();
			return containsRelation(parent) || containsRelation(model);
		} else {
			// we may can do better here...
			return false;
		}
	}

}
