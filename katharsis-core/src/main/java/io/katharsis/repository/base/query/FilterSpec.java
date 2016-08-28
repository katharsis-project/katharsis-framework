package io.katharsis.repository.base.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class FilterSpec extends AbstractPathSpec implements Serializable, Comparable<FilterSpec>, Cloneable {

	private static final long serialVersionUID = 8392755998245887525L;

	private FilterOperator operator;
	private Object value;
	private List<FilterSpec> expression;

	protected FilterSpec() {
	}

	FilterSpec(FilterSpec spec) {
		super(spec.getPath());
		this.operator = spec.operator;
		this.value = spec.value;
		this.expression = cloneExpressions(spec.expression);
	}

	public FilterSpec(FilterOperator operator, List<FilterSpec> expressions) {
		super();
		this.operator = operator;
		this.expression = expressions;
	}

	public FilterSpec(List<String> attributePath, FilterOperator operator, Object value) {
		super(attributePath);
		if (operator == null) {
			throw new IllegalArgumentException("Condition required");
		}
		if (value == null && operator != FilterOperator.EQ && operator != FilterOperator.NEQ) {
			throw new IllegalArgumentException("Value required for operation " + operator.id());
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

		this.attributePath = attributePath;
		this.operator = operator;
		this.value = value;
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
		// if nothing is set we assume an empty expression (i.e. an empty where
		// clause)
		return getPath() == null;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(new Object[] { getPath(), operator, expression, value });
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
		if (attributePath == null) {
			if (other.attributePath != null)
				return false;
		} else if (!attributePath.equals(other.attributePath))
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
		} else if (attributePath != null) {
			b.append('(');
			b.append(attributePath);
			b.append(operator.name());
			b.append(')');
		}
		return b.toString();
	}

	public static FilterSpec eq(List<String> attribute, Object value) {
		return new FilterSpec(attribute, FilterOperator.EQ, value);
	}

	public static FilterSpec neq(List<String> attribute, Object value) {
		return new FilterSpec(attribute, FilterOperator.NEQ, value);
	}

	public static FilterSpec like(List<String> attribute, Object value) {
		return new FilterSpec(attribute, FilterOperator.LIKE, value);
	}

	public static FilterSpec gt(List<String> attribute, Object value) {
		return new FilterSpec(attribute, FilterOperator.GT, value);
	}

	public static FilterSpec ge(List<String> attribute, Object value) {
		return new FilterSpec(attribute, FilterOperator.GE, value);
	}

	public static FilterSpec lt(List<String> attribute, Object value) {
		return new FilterSpec(attribute, FilterOperator.LT, value);
	}

	public static FilterSpec le(List<String> attribute, Object value) {
		return new FilterSpec(attribute, FilterOperator.LE, value);
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
	 * Should not be used outside of <code>FetchCriteria.normalize()</code>!<br>
	 * <br>
	 * Normalizes this FilterSpec by normalizing all FilterSpec objects within
	 * list <code>expression</code> and then sorting the list itself.
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

		if (attributePath != null && attributePath.equals(other.attributePath)) {
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

		if (attributePath != null) {
			return other.attributePath != null ? attributePath.toString().compareTo(other.attributePath.toString())
					: -1;
		}
		return other.attributePath == null ? 0 : 1;
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
