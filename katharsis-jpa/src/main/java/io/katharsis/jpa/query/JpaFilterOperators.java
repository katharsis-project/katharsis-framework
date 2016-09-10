package io.katharsis.jpa.query;

import java.util.HashSet;
import java.util.Set;

import io.katharsis.queryspec.FilterOperator;

public class JpaFilterOperators {

	private JpaFilterOperators() {
	}

	/**
	 * Case sensitive like operation
	 */
	public static final FilterOperator LIKE = new FilterOperator("LIKE") {

		@Override
		public boolean matches(Object value1, Object value2) {
			throw new UnsupportedOperationException(); // handle differently
		}

	};

	/**
	 * Case-insensitive like operation
	 */
	public static final FilterOperator ILIKE = new FilterOperator("ILIKE") {

		@Override
		public boolean matches(Object value1, Object value2) {
			throw new UnsupportedOperationException(); // handle differently
		}

	};

	/**
	 * Case-sensitive not like operation
	 */
	public static final FilterOperator NOT_LIKE = new FilterOperator("NOT_LIKE") {

		@Override
		public boolean matches(Object value1, Object value2) {
			throw new UnsupportedOperationException(); // handle differently
		}

	};

	public static Set<FilterOperator> getSupportedOperators() {
		Set<FilterOperator> set = new HashSet<>();
		set.add(LIKE);
		set.add(ILIKE);
		set.add(NOT_LIKE);
		set.add(FilterOperator.EQ);
		set.add(FilterOperator.NEQ);
		set.add(FilterOperator.GT);
		set.add(FilterOperator.GE);
		set.add(FilterOperator.LT);
		return set;
	}

	public static FilterOperator getDefaultOperator() {
		return FilterOperator.EQ;
	}

}
