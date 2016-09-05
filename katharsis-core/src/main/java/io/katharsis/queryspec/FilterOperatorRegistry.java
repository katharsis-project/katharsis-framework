package io.katharsis.queryspec;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FilterOperatorRegistry {

	private Map<String, FilterOperator> operators = new ConcurrentHashMap<>();

	private FilterOperator defaultOperator;

	public FilterOperator get(String id) {
		FilterOperator op = operators.get(id);
		if (op != null) {
			return op;
		} else {
			throw new IllegalArgumentException("operator " + id + " unknown");
		}
	}

	/**
	 * Sets the default operator, usually {@link FilterOperator#EQ}.
	 * 
	 * @param defaultOperator default operator
	 */
	public void setDefaultOperator(FilterOperator defaultOperator) {
		this.defaultOperator = defaultOperator;
	}

	/**
	 * @return operator to use if none of explicitly stated.
	 */
	public FilterOperator getDefaultOperator() {
		if (defaultOperator == null) {
			throw new IllegalStateException("no default operator provided");
		}
		return defaultOperator;
	}

	/**
	 * Register an new operator.
	 * 
	 * @param operator new operator
	 */
	public void register(FilterOperator operator) {
		if (operators.containsKey(operator.getName()) && !operator.equals(operators.get(operator.getName()))) {
			throw new IllegalStateException("operator with name " + operator.getName() + " already registered");
		}
		operators.put(operator.getName(), operator);
	}

	public Collection<FilterOperator> getAll() {
		return Collections.unmodifiableCollection(operators.values());
	}
}
