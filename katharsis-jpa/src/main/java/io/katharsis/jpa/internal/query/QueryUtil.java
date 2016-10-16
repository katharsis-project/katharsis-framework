package io.katharsis.jpa.internal.query;

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
import io.katharsis.queryspec.SortSpec;

public class QueryUtil {

	private QueryUtil() {
	}

	private static List<String> toEntityPath(MetaProjection metaProjection, List<String> attributePath) {
		MetaAttributePath derivationPath = metaProjection.resolvePath(attributePath);

		List<String> entityPath = new ArrayList<>();
		for (MetaAttribute attr : derivationPath) {
			MetaAttributeProjection projAttr = (MetaAttributeProjection) attr;
			MetaAttributePath path = projAttr.getPath();
			for (MetaAttribute pathElement : path) {
				entityPath.add(pathElement.getName());
			}
		}
		return entityPath;
	}

	public static SortSpec toEntitySpec(MetaDataObject metaObject, SortSpec sortSpec) {
		if (metaObject instanceof MetaProjection) {
			MetaProjection metaProjection = (MetaProjection) metaObject;
			return new SortSpec(toEntityPath(metaProjection, sortSpec.getAttributePath()), sortSpec.getDirection());
		} else {
			return sortSpec;
		}
	}

	private static List<SortSpec> toEntitySpec(MetaDataObject meta, List<SortSpec> sortSpecs) {
		if (meta instanceof MetaProjection) {
			List<SortSpec> entitySortSpec = new ArrayList<>();
			for (SortSpec SortSpec : sortSpecs) {
				entitySortSpec.add(toEntitySpec(meta.asProjection(), SortSpec));
			}
			return entitySortSpec;
		} else {
			return sortSpecs;
		}
	}

	public static boolean hasTotalOrder(MetaDataObject meta, List<SortSpec> sortSpecs) {
		if (meta instanceof MetaProjection) {
			MetaProjection projectionMeta = (MetaProjection) meta;
			List<SortSpec> entitySortSpecs = toEntitySpec(meta, sortSpecs);
			return hasTotalOrder(projectionMeta.getBaseType(), entitySortSpecs);
		} else {
			boolean hasTotalOrder = contains(meta.getPrimaryKey(), sortSpecs);
			if (hasTotalOrder)
				return true;
			for (MetaKey key : meta.getKeys()) {
				if (key.isUnique() && contains(key, sortSpecs)) {
					return true;
				}
			}
			return false;
		}
	}

	public static boolean contains(MetaKey key, List<SortSpec> entitySortSpecs) {
		for (MetaAttribute attr : key.getElements()) {
			boolean contains = false;
			for (SortSpec sortSpec : entitySortSpecs) {
				List<String> sortAttrPath = sortSpec.getAttributePath();
				if (sortAttrPath.size() == 1 && sortAttrPath.get(0).equals(attr.getName())) {
					contains = true;
					break;
				}
			}
			if (!contains)
				return false;
		}
		return true;

	}

	public static boolean hasManyRootsFetchesOrJoins(CriteriaQuery<?> criteriaQuery) {
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

	public static boolean containsRelation(Object expr) {
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
