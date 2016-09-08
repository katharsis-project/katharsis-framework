package io.katharsis.jpa.query;

import io.katharsis.queryspec.FilterOperator;
import io.katharsis.queryspec.FilterOperatorRegistry;

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

	public static void setup(FilterOperatorRegistry registry) {
		registry.register(LIKE);
		registry.register(ILIKE);
		registry.register(NOT_LIKE);
		registry.register(FilterOperator.EQ);
		registry.register(FilterOperator.NEQ);
		registry.register(FilterOperator.GT);
		registry.register(FilterOperator.GE);
		registry.register(FilterOperator.LT);
		registry.register(FilterOperator.LE);

		registry.setDefaultOperator(FilterOperator.EQ);
		registry.setDefaultOperator(FilterOperator.EQ);
	}

}
