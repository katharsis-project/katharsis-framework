package io.katharsis.jpa.internal.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class FilterSpec implements Serializable, Comparable<FilterSpec>, Cloneable {


	private String attributeName;

	private FilterOperator operator = FilterOperator.AND;

	private Object value;

	private List<FilterSpec> expression;

	protected FilterSpec() {
	}

	FilterSpec(FilterSpec spec) {
		this.attributeName = spec.attributeName;
		this.operator = spec.operator;
		this.value = spec.value;
		this.expression = cloneExpressions(spec.expression);
	}

	public FilterSpec(FilterOperator operator, List<FilterSpec> expressions) {
		this.operator = operator;
		this.expression = expressions;
	}

	public FilterSpec(String attributeName, FilterOperator operator, Object value) {
		if (attributeName == null || attributeName.length() == 0) {
			throw new IllegalArgumentException("Null or empty 'attributeName' not allowed");
		}
		if (operator == null) {
			throw new IllegalArgumentException("Condition required");
		}
		if (value == null && operator != FilterOperator.EQUAL && operator != FilterOperator.NOT_EQUAL) {
			throw new IllegalArgumentException("Value required for operation " + operator.name());
		}
		if (operator == FilterOperator.NOT) {
			throw new IllegalArgumentException("NOT operator not allowed when comparing with a value, use NOT_EQUAL");
		}
		if (operator == FilterOperator.AND) {
			throw new IllegalArgumentException("AND operator not allowed when comparing with a value");
		}
		if (operator == FilterOperator.OR) {
			throw new IllegalArgumentException("OR operator not allowed when comparing with a value");
		}

		this.attributeName = attributeName;
		this.operator = operator;
		this.value = value;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public FilterOperator getOperator() {
		return operator;
	}

	public void setOperator(FilterOperator condition) {
		this.operator = condition;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public List<FilterSpec> getExpression() {
		return expression;
	}

	/**
	 * Adds the given expression to the expression list and returns itself.
	 * 
	 * @param expr
	 * @return
	 */
	public FilterSpec addExpression(FilterSpec expr) {
		if (expression == null) {
			expression = new ArrayList<FilterSpec>();
		}
		expression.add((FilterSpec) expr);
		return this;
	}

	public boolean hasExpressions() {
		// if nothing is set we assume an empty expression (i.e. an empty where clause)
		return attributeName == null;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(new Object[] { attributeName, operator, expression, value });
	}

	private boolean containsEquivalentValue(Collection<Object> collection, Object value) {
		for (Object o : collection) {
			if (isEquals(value, o)) {
				return true;
			}
		}
		return false;
	}

	private boolean equivalentCollections(Collection<Object> val1, Collection<Object> val2) {
		if (val1.size() != val2.size()) {
			return false;
		}
		if (val1 instanceof List<?> && val2 instanceof List<?>) {
			Iterator<Object> it1 = ((List<Object>) val1).iterator();
			Iterator<Object> it2 = ((List<Object>) val2).iterator();
			while (it1.hasNext()) {
				if (!isEquals(it1.next(), it2.next())) {
					return false;
				}
			}
		} else {
			Iterator<Object> it1 = val1.iterator();
			while (it1.hasNext()) {
				if (!containsEquivalentValue(val2, it1.next())) {
					return false;
				}
			}
		}
		return true;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean isEquals(Object val1, Object val2) {
		if (val1 == null) {
			return val2 == null;
		}
		if (val2 == null) {
			return false;
		}
		if (val1 instanceof Collection) {
			if (!(val2 instanceof Collection)) {
				return false;
			}
			return equivalentCollections((Collection<Object>) val1, (Collection<Object>) val2);
		}
		if (val1 instanceof Comparable && val2 instanceof Comparable) {
			return ((Comparable) val1).compareTo((Comparable) val2) == 0;
		}
		return val1.equals(val2);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FilterSpec other = (FilterSpec) obj;
		if (attributeName == null) {
			if (other.attributeName != null)
				return false;
		} else if (!attributeName.equals(other.attributeName))
			return false;
		if (operator != other.operator)
			return false;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (compareExpressions(expression, other.expression) != 0)
			return false;
		return isEquals(value, other.value);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		if (getExpression() != null) {
			int nExprs = getExpression().size();
			if (getOperator() == FilterOperator.NOT) {
				b.append("NOT");
				if (nExprs == 0)
					b.append("(<***NOEXPRESSION***>)");
				else {
					if (nExprs > 1)
						b.append('(');
					b.append(getExpression().get(0));
					for (int i = 1; i < nExprs; i++) {
						b.append(" AND ");
						b.append(getExpression().get(i));
					}
					if (nExprs > 1)
						b.append(')');
				}
			} else {
				if (nExprs > 1)
					b.append('(');
				b.append(getExpression().get(0));
				for (int i = 1; i < nExprs; i++) {
					b.append(' ');
					b.append(getOperator());
					b.append(' ');
					b.append(getExpression().get(i));
				}
				if (nExprs > 1)
					b.append(')');
			}
		} else if (attributeName != null) {
			b.append('(');
			b.append(attributeName);
			b.append(operator.getOpWithValue(value));
			b.append(')');
		}
		return b.toString();
	}

	public static FilterSpec eq(String attribute, Object value) {
		return new FilterSpec(attribute, FilterOperator.EQUAL, value);
	}

	public static FilterSpec neq(String attribute, Object value) {
		return new FilterSpec(attribute, FilterOperator.NOT_EQUAL, value);
	}

	public static FilterSpec like(String attribute, Object value) {
		return new FilterSpec(attribute, FilterOperator.LIKE, value);
	}

	public static FilterSpec nlike(String attribute, Object value) {
		return new FilterSpec(attribute, FilterOperator.NOT_LIKE, value);
	}

	public static FilterSpec gt(String attribute, Object value) {
		return new FilterSpec(attribute, FilterOperator.GREATER, value);
	}

	public static FilterSpec ge(String attribute, Object value) {
		return new FilterSpec(attribute, FilterOperator.GREATER_EQUAL, value);
	}

	public static FilterSpec lt(String attribute, Object value) {
		return new FilterSpec(attribute, FilterOperator.LESS, value);
	}

	public static FilterSpec le(String attribute, Object value) {
		return new FilterSpec(attribute, FilterOperator.LESS_EQUAL, value);
	}

	public static FilterSpec and(Collection<FilterSpec> conditions) {
		return and(conditions.toArray(new FilterSpec[conditions.size()]));
	}

	public static FilterSpec and(FilterSpec... conditions) {
		if (conditions.length == 1) {
			return conditions[0];
		}
		FilterSpec ret = new FilterSpec();
		ret.setOperator(FilterOperator.AND);
		for (FilterSpec c : conditions) {
			ret.addExpression(c);
		}
		return ret;
	}

	public static FilterSpec or(Collection<FilterSpec> conditions) {
		return or(conditions.toArray(new FilterSpec[conditions.size()]));
	}

	public static FilterSpec or(FilterSpec... conditions) {
		if (conditions.length == 1) {
			return conditions[0];
		}
		FilterSpec ret = new FilterSpec();
		ret.setOperator(FilterOperator.OR);
		for (FilterSpec c : conditions) {
			ret.addExpression(c);
		}
		return ret;
	}

	public static FilterSpec not(FilterSpec expression) {
		FilterSpec ret = new FilterSpec();
		ret.setOperator(FilterOperator.NOT);
		ret.addExpression(expression);
		return ret;
	}

	/**
	 * Case insensitive like.
	 */
	public static FilterSpec ilike(String attribute, Object value) {
		return new FilterSpec(attribute, FilterOperator.ILIKE, value);
	}

	/**
	 * Contains / Member of
	 */
	public static FilterSpec contains(String attribute, Object value) {
		return new FilterSpec(attribute, FilterOperator.CONTAINS, value);
	}

	/**
	 * Should not be used outside of <code>FetchCriteria.normalize()</code>!<br>
	 * <br>
	 * Normalizes this FilterSpec by normalizing all FilterSpec objects within list <code>expression</code> and then
	 * sorting the list itself.
	 */
	void normalize() {
		if (expression != null) {
			for (FilterSpec filterSpec : expression) {
				filterSpec.normalize();
			}
			Collections.sort(expression);
		}
	}

	@Override
	public int compareTo(FilterSpec other) {
		if (this.equals(other)) {
			return 0;
		}

		if (other == null) {
			return -1;
		}

		if (attributeName != null && attributeName.equals(other.attributeName)) {
			if (operator != null && operator.equals(other.operator)) {
				if (value != null && value.equals(other.value)) {
					return compareExpressions(expression, other.expression);
				} else if (value != null) {
					return other.value != null ? value.toString().compareTo(other.value.toString()) : -1;
				}
				return other.value == null ? 0 : 1;
			}
			if (operator != null) {
				return other.operator != null ? operator.compareTo(other.operator) : -1;
			}
			return other.operator == null ? 0 : 1;
		}

		if (attributeName != null) {
			return other.attributeName != null ? attributeName.compareTo(other.attributeName) : -1;
		}
		return other.attributeName == null ? 0 : 1;
	}

	private int compareExpressions(List<FilterSpec> expression, List<FilterSpec> otherExpression) {
		String expressions = duplicateAndNormalize(expression);
		String otherExpressions = duplicateAndNormalize(otherExpression);
		return expressions.compareTo(otherExpressions);
	}

	private String duplicateAndNormalize(List<FilterSpec> expression) {
		if (expression == null) {
			return "";
		}

		List<FilterSpec> duplicateExpression = new ArrayList<FilterSpec>(expression);
		for (FilterSpec filterSpec : duplicateExpression) {
			filterSpec.normalize();
		}
		return duplicateExpression.toString();
	}

	static List<FilterSpec> cloneExpressions(List<FilterSpec> list) {
		if (list == null) {
			return null;
		}
		List<FilterSpec> result = new ArrayList<FilterSpec>();
		for (FilterSpec spec : list) {
			result.add(new FilterSpec(spec));
		}
		return result;
	}

}
