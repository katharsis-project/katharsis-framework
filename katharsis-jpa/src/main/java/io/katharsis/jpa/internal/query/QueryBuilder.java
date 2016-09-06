package io.katharsis.jpa.internal.query;

import java.util.ArrayList;
import java.util.List;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaKey;
import io.katharsis.jpa.internal.query.backend.JpaQueryBackend;
import io.katharsis.queryspec.FilterSpec;

public class QueryBuilder<T, F, O, P, E> {

	private static final String SORT_COLUMN_ALIAS_PREFIX = "__sort";

	private JpaQueryBackend<F, O, P, E> backend;

	private AbstractJpaQueryImpl<T, ?> query;

	public QueryBuilder(AbstractJpaQueryImpl<T, ?> query, JpaQueryBackend<F, O, P, E> backend) {
		this.query = query;
		this.backend = backend;
	}

	/**
	 * Returns true if distinct is set manually or the "auto distinct" mode is
	 * enabled and the user performs a join or fetch on a many assocation. In
	 * this case, attributes from referenced entities included in the sort
	 * clause are also added to the select clause.
	 */
	protected int applyDistinct() {
		int numAutoSelections = 0;
		boolean distinct;
		if (query.autoDistinct) {
			// distinct for many join/fetches or manual
			// in case of ViewQuery we may not need this here, but we need to do
			// the selection of order-by columns below
			distinct = query.autoDistinct && !query.autoGroupBy && backend.hasManyRootsFetchesOrJoins();
			if (distinct) {
				// we also need to select sorted attributes (for references)
				numAutoSelections = addOrderExpressionsToSelection();
			}
		} else {
			distinct = query.distinct;
		}
		if (distinct) {
			backend.distinct();
		}
		return numAutoSelections;
	}

	private int addOrderExpressionsToSelection() {
		int numAutoSelections = 0;
		int prefixIndex = 0;
		List<O> newOrderList = new ArrayList<>();
		for (O order : backend.getOrderList()) {
			E expression = backend.getExpression(order);
			if (backend.containsRelation(expression)) {
				backend.addSelection(expression, SORT_COLUMN_ALIAS_PREFIX + prefixIndex++);
				numAutoSelections++;
			}
			newOrderList.add(order);
		}
		backend.setOrder(newOrderList);
		return numAutoSelections;
	}

	protected void applySortSpec() {
		QuerySortBuilder<T, E, O> orderBuilder = new QuerySortBuilder<>(query, backend);
		orderBuilder.applySortSpec();
	}

	protected void applyFilterSpec() {
		QueryFilterBuilder<P, F> predicateBuilder = new QueryFilterBuilder<>(query.getVirtualAttrs(), backend);

		MetaDataObject meta = query.getMeta();
		List<FilterSpec> filters = query.getFilterSpecs();
		List<P> predicates = predicateBuilder.filterSpecListToPredicateArray(meta, backend.getRoot(), filters);
		if (predicates != null && !predicates.isEmpty()) {
			backend.addPredicate(backend.and(predicates));
		}

		MetaAttribute parentAttr = query.getParentAttr();
		if (parentAttr != null) {
			MetaEntity parentEntity = parentAttr.getParent().asEntity();
			MetaKey primaryKey = parentEntity.getPrimaryKey();
			MetaAttribute primaryKeyAttr = primaryKey.getUniqueElement();

			backend.addParentPredicate(primaryKeyAttr);
		}
	}
}
