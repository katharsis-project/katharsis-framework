package io.katharsis.queryspec;

import java.util.*;
import java.util.stream.Stream;

import io.katharsis.core.internal.utils.PropertyUtils;
import io.katharsis.resource.list.ResourceList;
import io.katharsis.resource.meta.MetaInformation;
import io.katharsis.resource.meta.PagedMetaInformation;

/**
 * Applies the given QuerySpec to the provided list in memory. Result available
 * with getResult(). Use QuerySpec.apply to make use of this class.
 */
public class InMemoryEvaluator {

	public <T> void eval(Iterable<T> resources, QuerySpec querySpec, ResourceList<T> resultList) {

		for (T resource : resources) {
			resultList.add(resource);
		}

		// filter
		if (!querySpec.getFilters().isEmpty()) {
			FilterSpec filterSpec = FilterSpec.and(querySpec.getFilters());
			applyFilter(resultList, filterSpec);
		}
		long totalCount = resultList.size();

		// sort
		resultList.sort(new SortSpecComparator<T>(querySpec.getSort()));

		// offset/limit
		applyPaging(resultList, querySpec);

		// set page information
		if (querySpec.getLimit() != null || querySpec.getOffset() != 0) {
			MetaInformation meta = resultList.getMeta();
			if (meta instanceof PagedMetaInformation) {
				PagedMetaInformation pagedMeta = (PagedMetaInformation) meta;
				pagedMeta.setTotalResourceCount(totalCount);
			}
		}
	}


	private <T> void applyPaging(List<T> results, QuerySpec querySpec) {
		int offset = (int) Math.min(querySpec.getOffset(), Integer.MAX_VALUE);
		int limit = (int) Math.min(Integer.MAX_VALUE, querySpec.getLimit() != null ? querySpec.getLimit() : Integer.MAX_VALUE);
		limit = Math.min(results.size() - offset, limit);
		if (offset > 0 || limit < results.size()) {
			List<T> subList = new ArrayList<>(results.subList(offset, offset + limit));
			results.clear();
			results.addAll(subList);
		}
	}

	private <T> void applyFilter(List<T> results, FilterSpec filterSpec) {
		Optional.ofNullable(filterSpec).ifPresent(fs -> results.removeIf(next ->  !matches(next,fs)));
	}

	public static boolean matches(Object object, FilterSpec filterSpec) {
		List<FilterSpec> expressions = filterSpec.getExpression();
		if (expressions == null) {
			return matchesPrimitiveOperator(object, filterSpec);
		}
		if (filterSpec.getOperator() == FilterOperator.OR) {
			return expressions.stream().anyMatch(e -> matches(object,e));
		}
		if (filterSpec.getOperator() == FilterOperator.AND) {
			return expressions.stream().allMatch(e -> matches(object, e));
		}
		if (filterSpec.getOperator() == FilterOperator.NOT) {
			return expressions.stream().noneMatch(e -> matches(object, e));
		}
		throw new UnsupportedOperationException("not implemented " + filterSpec);
	}

	private static boolean matchesPrimitiveOperator(Object object, FilterSpec filterSpec) {
		Object value = PropertyUtils.getProperty(object, filterSpec.getAttributePath());
		FilterOperator operator = filterSpec.getOperator();
		Object filterValue = filterSpec.getValue();
		boolean result = false;
		// the ability to operate on a null value should be handled polymorphically by the operator itself
		if (filterValue != null || ((operator.equals(FilterOperator.EQ) || operator.equals(FilterOperator.NEQ)))) {
			result = (value instanceof Collection ? ((Collection<?>)value).stream() : Stream.of(value)).anyMatch(v -> operator.matches(v, filterValue));
		}

		return result;
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
				Comparable<Object> value1 = (Comparable<Object>) PropertyUtils.getProperty(o1, orderSpec.getAttributePath());
				Comparable<Object> value2 = (Comparable<Object>) PropertyUtils.getProperty(o2, orderSpec.getAttributePath());

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
