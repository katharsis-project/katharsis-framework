package io.katharsis.queryspec;

import io.katharsis.utils.CompareUtils;

/**
 * Filter operator used to compare attributes to values by {@link FilterSpec}.
 */
public abstract class FilterOperator {

	/**
	 * Boolean and
	 */
	public static final FilterOperator AND = new FilterOperator("AND") {

		@Override
		public boolean matches(Object value1, Object value2) {
			throw new UnsupportedOperationException(); // handle differently
		}

	};
	/**
	 * Boolean or
	 */
	public static final FilterOperator OR = new FilterOperator("OR") {

		@Override
		public boolean matches(Object value1, Object value2) {
			throw new UnsupportedOperationException(); // handle differently
		}

	};

	/**
	 * Boolean not
	 */
	public static final FilterOperator NOT = new FilterOperator("NOT") {

		@Override
		public boolean matches(Object value1, Object value2) {
			throw new UnsupportedOperationException(); // handle differently
		}

	};

	/**
	 * equals
	 */
	public static final FilterOperator EQ = new FilterOperator("EQ") {

		@Override
		public boolean matches(Object value1, Object value2) {
			return CompareUtils.isEquals(value1, value2);
		}

	};

	/**
	 * like with * as wildcard
	 */
	public static final FilterOperator LT = new FilterOperator("LT") {

		@SuppressWarnings("unchecked")
		@Override
		public boolean matches(Object value1, Object value2) {
			Comparable<Object> c1 = (Comparable<Object>) value1;
			Comparable<Object> c2 = (Comparable<Object>) value2;
			return c1 != null && c1.compareTo(c2) < 0;
		}

	};

	/**
	 * less equals
	 */
	public static final FilterOperator LE = new FilterOperator("LE") {

		@SuppressWarnings("unchecked")
		@Override
		public boolean matches(Object value1, Object value2) {
			Comparable<Object> c1 = (Comparable<Object>) value1;
			Comparable<Object> c2 = (Comparable<Object>) value2;
			return c1 != null && c1.compareTo(c2) <= 0;
		}

	};

	/**
	 * greater
	 */
	public static final FilterOperator GT = new FilterOperator("GT") {

		@SuppressWarnings("unchecked")
		@Override
		public boolean matches(Object value1, Object value2) {
			Comparable<Object> c1 = (Comparable<Object>) value1;
			Comparable<Object> c2 = (Comparable<Object>) value2;
			return c1 != null && c1.compareTo(c2) > 0;
		}
	};

	/**
	 * greater equals
	 */
	public static final FilterOperator GE = new FilterOperator("GE") {

		@SuppressWarnings("unchecked")
		@Override
		public boolean matches(Object value1, Object value2) {
			Comparable<Object> c1 = (Comparable<Object>) value1;
			Comparable<Object> c2 = (Comparable<Object>) value2;
			return c1 != null && c1.compareTo(c2) >= 0;
		}
	};

	/**
	 * not equals
	 */
	public static final FilterOperator NEQ = new FilterOperator("NEQ") {

		@Override
		public boolean matches(Object value1, Object value2) {
			return !CompareUtils.isEquals(value1, value2);
		}
	};

	private String id;

	protected FilterOperator(String id) {
		this.id = id;
	}

	public String name() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FilterOperator other = (FilterOperator) obj;
		return id.equals(other.id);
	}

	@Override
	public String toString() {
		return id;
	}

	public String getName() {
		return id;
	}

	public int compareTo(FilterOperator operator) {
		return id.compareTo(operator.id);
	}

	/**
	 * Performs a in-memory evaluation of the operator on the given to values.
	 */
	public abstract boolean matches(Object value1, Object value2);

}
