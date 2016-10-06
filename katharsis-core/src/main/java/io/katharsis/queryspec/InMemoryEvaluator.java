package io.katharsis.queryspec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import io.katharsis.response.paging.PagedResultList;
import io.katharsis.utils.PropertyUtils;

/**
 * Applies the given QuerySpec to the provided list in memory. Result available
 * with getResult(). Use QuerySpec.apply to make use of this class.
 */
public class InMemoryEvaluator {

	public <T> List<T> eval(Iterable<T> resources, QuerySpec querySpec) {
		List<T> results = new ArrayList<>();
		
		Iterator<T> iterator = resources.iterator();
		while(iterator.hasNext()){
			results.add(iterator.next());
		}
		long totalCount = results.size();

		// filter
		if (!querySpec.getFilters().isEmpty()) {
			FilterSpec filterSpec = FilterSpec.and(querySpec.getFilters());
			applyFilter(results, filterSpec);
		}

		// sort
		applySorting(results, querySpec.getSort());

		// offset/limit
		results = applyPaging(results, querySpec);

		return new PagedResultList<>(results, totalCount);
	}

	private <T> void applySorting(List<T> results, List<SortSpec> sortSpec) {
		if (!sortSpec.isEmpty()) {
			Collections.sort(results, new SortSpecComparator<>(sortSpec));
		}
	}

	private <T> List<T> applyPaging(List<T> results, QuerySpec querySpec) {
		int offset = (int) Math.min(querySpec.getOffset(), Integer.MAX_VALUE);
		int limit = (int) Math.min(Integer.MAX_VALUE,
				querySpec.getLimit() != null ? querySpec.getLimit() : Integer.MAX_VALUE);
		limit = Math.min(results.size() - offset, limit);
		if (offset > 0 || limit < results.size()) {
			return results.subList(offset, offset + limit);
		}
		return results;
	}

	private <T> void applyFilter(List<T> results, FilterSpec filterSpec) {
		if (filterSpec != null) {
			Iterator<T> iterator = results.iterator();
			while (iterator.hasNext()) {
				if (!matches(iterator.next(), filterSpec)) {
					iterator.remove();
				}
			}
		}
	}

	public static boolean matches(Object object, FilterSpec filterSpec) {
		List<FilterSpec> expressions = filterSpec.getExpression();
		if (expressions == null) {
			Object value = PropertyUtils.getProperty(object, filterSpec.getAttributePath());
			return filterSpec.getOperator().matches(value, filterSpec.getValue());
		} else if (filterSpec.getOperator() == FilterOperator.OR) {
			return matchesOr(object, expressions);
		} else if (filterSpec.getOperator() == FilterOperator.AND) {
			return matchesAnd(object, expressions);
		} else if (filterSpec.getOperator() == FilterOperator.NOT) {
			return !matches(object, FilterSpec.and(expressions));
		}
		throw new UnsupportedOperationException("not implemented " + filterSpec);
	}

	private static boolean matchesOr(Object object, List<FilterSpec> expressions) {
		for (FilterSpec expr : expressions) {
			if (matches(object, expr)) {
				return true;
			}
		}
		return false;
	}

	private static boolean matchesAnd(Object object, List<FilterSpec> expressions) {
		for (FilterSpec expr : expressions) {
			if (!matches(object, expr)) {
				return false;
			}
		}
		return true;
	}

	static class SortSpecComparator<T> implements Comparator<T> {

		private List<SortSpec> sortSpecs;

		public SortSpecComparator(List<SortSpec> sortSpecs) {
			this.sortSpecs = sortSpecs;
		}

		@Override
		@SuppressWarnings("unchecked")
		public int compare(T o1, T o2) {
			for (SortSpec orderSpec : sortSpecs) {
				Comparable<Object> value1 = (Comparable<Object>) PropertyUtils.getProperty(o1,
						orderSpec.getAttributePath());
				Comparable<Object> value2 = (Comparable<Object>) PropertyUtils.getProperty(o2,
						orderSpec.getAttributePath());

				int d = compare(value1, value2);
				if (orderSpec.getDirection() == Direction.DESC) {
					d = -d;
				}
				if (d != 0)
					return d;
			}
			return 0;
		}

		private int compare(Comparable<Object> value1, Comparable<Object> value2) {
			if (value1 == null && value2 == null)
				return 0;
			if (value1 == null)
				return -1;
			if (value2 == null)
				return 1;

			return value1.compareTo(value2);
		}
	}
}
