package io.katharsis.repository.base.query;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FilterOperator {

	private static Map<String, FilterOperator> operators = new ConcurrentHashMap<String, FilterOperator>();

	private static FilterOperator get(String id) {
		FilterOperator op = operators.get(id);
		if (op == null) {
			synchronized (FilterOperator.class) {
				op = operators.get(id);
				if (op == null) {
					op = new FilterOperator(id);
					operators.put(id, op);
				}
			}
		}
		return op;
	}

	/**
	 * Boolean and
	 */
	public static final FilterOperator AND = get("AND");
	/**
	 * Boolean or
	 */
	public static final FilterOperator OR = get("OR");

	/**
	 * Boolean not
	 */
	public static final FilterOperator NOT = get("NOT");

	/**
	 * equals
	 */
	public static final FilterOperator EQ = get("EQ");

	/**
	 * like
	 */
	public static final FilterOperator LIKE = get("LIKE");

	/**
	 * like with * as wildcard
	 */
	public static final FilterOperator LT = get("LT");

	/**
	 * less equals
	 */
	public static final FilterOperator LE = get("LE");

	/**
	 * greater
	 */
	public static final FilterOperator GT = get("GT");

	/**
	 * greater equals
	 */
	public static final FilterOperator GE = get("GE");

	/**
	 * not equals
	 */
	public static final FilterOperator NEQ = get("NEQ");

	private String id;

	private FilterOperator(String id) {
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
		return !id.equals(other.id);
	}

	@Override
	public String toString() {
		return id;
	}

	public String id() {
		return id;
	}

	public int compareTo(FilterOperator operator) {
		return id.compareTo(operator.id);
	}

}
