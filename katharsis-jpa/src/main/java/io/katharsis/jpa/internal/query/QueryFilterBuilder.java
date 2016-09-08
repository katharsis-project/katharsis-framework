package io.katharsis.jpa.internal.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.JoinType;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaAttributeFinder;
import io.katharsis.jpa.internal.meta.MetaAttributePath;
import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.meta.MetaMapType;
import io.katharsis.jpa.internal.meta.MetaProjection;
import io.katharsis.jpa.internal.meta.MetaType;
import io.katharsis.jpa.internal.query.backend.JpaQueryBackend;
import io.katharsis.jpa.query.AnyTypeObject;
import io.katharsis.queryspec.FilterOperator;
import io.katharsis.queryspec.FilterSpec;

public final class QueryFilterBuilder<P, F> {

	private static final int ORACLE_PARAM_LIMIT = 900;

	private MetaAttributeFinder attributeFinder;

	private JpaQueryBackend<F, ?, P, ?> backend;

	protected QueryFilterBuilder(final VirtualAttributeRegistry virtualAttrs, JpaQueryBackend<F, ?, P, ?> backend, MetaAttributeFinder attributeFinder) {
		this.backend = backend;
		this.attributeFinder = attributeFinder;
	}

	public List<P> filterSpecListToPredicateArray(MetaDataObject rootMeta, F root, List<FilterSpec> rowFilters) {
		return filterSpecListToPredicateArray(rootMeta, root, rowFilters, false, null);
	}

	public List<P> filterSpecListToPredicateArray(MetaDataObject rootMeta, F root, List<FilterSpec> rowFilters,
			boolean forceEntityBased, JoinType defaultPredicateJoinType) {
		ArrayList<P> predicateList = new ArrayList<>();
		for (FilterSpec rowFilter : rowFilters) {
			predicateList.add(
					filterSpecListToPredicate(rootMeta, root, rowFilter, forceEntityBased, defaultPredicateJoinType));
		}
		return predicateList;
	}

	protected P filterSpecListToPredicate(MetaDataObject rootMeta, F root, FilterSpec fs) {
		return filterSpecListToPredicate(rootMeta, root, fs, false, null);
	}

	protected P filterSpecListToPredicate(MetaDataObject rootMeta, F root, FilterSpec fs, boolean forceEntityBased,
			JoinType defaultPredicateJoinType) {
		if ((fs.getOperator() == FilterOperator.EQ || fs.getOperator() == FilterOperator.NEQ)
				&& fs.getValue() instanceof Collection && ((Collection<?>) fs.getValue()).size() > ORACLE_PARAM_LIMIT) {

			return filterLargeValueSets(fs, rootMeta, root, forceEntityBased, defaultPredicateJoinType);
		} else {
			if (fs.hasExpressions()) {
				return filterExpressions(fs, rootMeta, root, forceEntityBased, defaultPredicateJoinType);
			}

			else {
				return filterSimpleOperation(fs, rootMeta, forceEntityBased);
			}
		}
	}

	/**
	 * Split filters with two many value possibilities. For example, Oracle
	 * cannot handle more than 1000.
	 * 
	 * @param fs
	 * @param rootMeta
	 * @param root
	 * @param forceEntityBased
	 * @param defaultPredicateJoinType
	 * @return
	 */
	private P filterLargeValueSets(FilterSpec fs, MetaDataObject rootMeta, F root, boolean forceEntityBased,
			JoinType defaultPredicateJoinType) {
		ArrayList<FilterSpec> specs = new ArrayList<>();
		List<?> list = new ArrayList<>((Collection<?>) fs.getValue());
		for (int i = 0; i < list.size(); i += ORACLE_PARAM_LIMIT) {
			int nextOffset = i + Math.min(list.size() - i, ORACLE_PARAM_LIMIT);
			List<?> batchList = list.subList(i, nextOffset);
			specs.add(new FilterSpec(fs.getAttributePath(), fs.getOperator(), batchList));
		}

		FilterSpec orSpec = FilterSpec.or(specs);
		return filterSpecListToPredicate(rootMeta, root, orSpec, forceEntityBased, defaultPredicateJoinType);
	}

	private P filterSimpleOperation(FilterSpec fs, MetaDataObject rootMeta, boolean forceEntityBased) {
		Object value = fs.getValue();
		if (value instanceof Set) {
			// HashSet not properly supported in ORM/JDBC, convert to
			// list
			Set<?> set = (Set<?>) value;
			value = new ArrayList<Object>(set);
		}
		MetaDataObject rootBaseType = getBaseType(rootMeta, forceEntityBased);
		MetaAttributePath path = rootBaseType.resolvePath(fs.getAttributePath(), attributeFinder);
		path = enhanceAttributePath(path, value);
		return backend.buildPredicate(fs.getOperator(), path, value);
	}

	private P filterExpressions(FilterSpec fs, MetaDataObject rootMeta, F root, boolean forceEntityBased,
			JoinType defaultPredicateJoinType) {
		// and, or, not.
		if (fs.getOperator() == FilterOperator.NOT) {
			return backend.not(backend.and(filterSpecListToPredicateArray(rootMeta, root, fs.getExpression(),
					forceEntityBased, defaultPredicateJoinType)));
		} else if (fs.getOperator() == FilterOperator.AND) {
			return backend.and(filterSpecListToPredicateArray(rootMeta, root, fs.getExpression(), forceEntityBased,
					defaultPredicateJoinType));
		} else if (fs.getOperator() == FilterOperator.OR) {
			return backend.or(filterSpecListToPredicateArray(rootMeta, root, fs.getExpression(), forceEntityBased,
					defaultPredicateJoinType));
		} else {
			throw new IllegalArgumentException(fs.toString());
		}
	}

	public MetaAttributePath enhanceAttributePath(MetaAttributePath attrPath, Object value) {
		MetaAttribute attr = attrPath.getLast();

		MetaType valueType = attr.getType();
		if (valueType instanceof MetaMapType) {
			valueType = ((MetaMapType) valueType).getValueType();
		}

		boolean anyType = AnyTypeObject.class.isAssignableFrom(valueType.getImplementationClass());
		if (anyType) {
			// we have and AnyType and do need to select the proper attribute of
			// the embeddable
			MetaAttribute anyAttr = AnyUtils.findAttribute((MetaDataObject) valueType, value);
			return attrPath.concat(anyAttr);
		} else {
			return attrPath;
		}
	}

	public static MetaDataObject getBaseType(MetaDataObject meta, boolean forceEntityBased) {
		if (forceEntityBased && meta instanceof MetaProjection) {
			return ((MetaProjection) meta).getBaseType();
		} else {
			return meta;
		}
	}

}
