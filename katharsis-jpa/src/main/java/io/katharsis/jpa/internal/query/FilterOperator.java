package io.katharsis.jpa.internal.query;

import java.util.Collection;

public enum FilterOperator {

	EQUAL		(" = ", 		" IS NULL ",		" IN ", 		"equal", 				Object.class),
	NOT_EQUAL	(" <> ",		" IS NOT NULL ",	" NOT IN ", 	"notEqual", 			Object.class),
	LIKE		(" LIKE ",		" IS NULL ", 		null, 			"like", 				String.class),
	NOT_LIKE	(" NOT LIKE ",	" IS NOT NULL ", 	null, 			"notLike", 				String.class ),
	GREATER		(" > ",			" IS NULL ", 		null, 			"greaterThan", 			java.lang.Comparable.class),
	LESS		(" < ",			" IS NULL ", 		null, 			"lessThan", 			java.lang.Comparable.class),
	GREATER_EQUAL(" >= ",		" IS NULL ", 		null, 			"greaterThanOrEqualTo", java.lang.Comparable.class),
	LESS_EQUAL	(" <= ",		" IS NULL ", 		null, 			"lessThanOrEqualTo", 	java.lang.Comparable.class),
	CONTAINS    (" MEMBER OF ", null,               null,           "isMember",             Object.class, true),
	AND         ( " AND ",		" AND ", null, "and", null),
	OR          ( " OR ",		" OR ",  null, "or", null),
	NOT         ( " NOT ",		" NOT ", null, "not", null),
	ILIKE		(" LIKE ",		" IS NULL ", 		null, 			"ilike", 				String.class),
	;

	private String forSingleValue;
	private String forNullValue;
	private String forCollectionValue;
	private String queryBuilderMethod; 
	private Class<?> jpaParam2Class;
	private boolean reverseArgumentOrder; 

	private FilterOperator(String forSingleValue, String forNullValue, String forCollectionValue, String queryBuilderMethod, Class<?> jpaParam2Class) {
		this.forSingleValue = forSingleValue;
		this.forNullValue = forNullValue;
		this.forCollectionValue = forCollectionValue;
		this.queryBuilderMethod = queryBuilderMethod;
		this.jpaParam2Class = jpaParam2Class;
	}

	private FilterOperator(String forSingleValue, String forNullValue, String forCollectionValue, String jpaQBuilderMethod, Class<?> jpaParam2Class,
			boolean reverseOrder) {
		this(forSingleValue, forNullValue, forCollectionValue, jpaQBuilderMethod, jpaParam2Class);
		this.reverseArgumentOrder = reverseOrder;
	}

	public String getOpWithValue(Object value) {
		if (value == null)
			return forNullValue;
		if (value instanceof Collection)
			return forCollectionValue + value;
		if (value instanceof String)
			return forSingleValue + '\'' + (String) value + '\'';
		return forSingleValue + value;
	}

	public String getOpWithParam(String param, Object value) {
		if (value == null)
			return forNullValue;
		if (hasReverseOrder()) {
			if (value instanceof Collection)
				return param + forCollectionValue;
			return param + forSingleValue;
		} else {
			if (value instanceof Collection)
				return forCollectionValue + param;
			return forSingleValue + param;
		}
	}

	// @deprecated use serializable values only
	// public String getOpWithParam(String param, Object value) {
	// return getOpWithParam(":" + param, (Serializable)value);
	// }

	public String getJpaQBuilderMethod() {
		return queryBuilderMethod;
	}

	public Class<?> getJpaQBuilderParam2Class() {
		return jpaParam2Class;
	}

	public boolean acceptsCollection() {
		return forCollectionValue != null;
	}

	public boolean hasReverseOrder() {
		return reverseArgumentOrder;
	}
}
