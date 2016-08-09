package io.katharsis.jpa.internal.query.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Predicate;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaAttributePath;
import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.meta.MetaMapType;
import io.katharsis.jpa.internal.meta.MetaProjection;
import io.katharsis.jpa.internal.meta.MetaType;
import io.katharsis.jpa.internal.query.AnyTypeObject;
import io.katharsis.jpa.internal.query.FilterOperator;
import io.katharsis.jpa.internal.query.FilterSpec;

public abstract class JpaPredicateBuilder {

	private static final int ORACLE_PARAM_LIMIT = 900;

	private static Set<FilterOperator> equalityOperator = EnumSet.of(FilterOperator.EQUAL, FilterOperator.NOT_EQUAL);

	private static Set<FilterOperator> likeOperator = EnumSet.of(FilterOperator.LIKE, FilterOperator.NOT_LIKE);

	private CriteriaBuilder cb;

	public JpaPredicateBuilder(CriteriaBuilder cb) {
		this.cb = cb;
	}

	public CriteriaBuilder getCb() {
		return cb;
	}

	public Predicate ilike(Expression<String> expr, String val) {
		return cb.like(cb.lower(expr), val.toLowerCase());
	}

	// public Predicate isMember(Expression elem, Expression collection) {
	//
	// logger.info("elemClass: {}, collectionClass: {}" + elem.getClass().getName(), collection.getClass().getName());
	// logger.info("elemIsCollection: {}, collectionIsCollection: {}",
	// Collection.class.isAssignableFrom(elem.getClass()), Collection.class.isAssignableFrom(collection.getClass()));
	// return cb.isMember(elem, collection);
	// }

	private Predicate negateIfNeeded(Predicate p, FilterOperator fc) {
		if (fc.equals(FilterOperator.NOT_EQUAL))
			return cb.not(p);
		return p;
	}

	/**
	 * Helper method to build Predicates from FetchCriteria conditions
	 * 
	 * @return built Predicate
	 *
	 */
	@Deprecated // move to some internal package
	public Predicate buildPredicate(FilterOperator operator, Expression<?> expression, Object value) {
		Object builder = this;
		Method m = null;
		try {
			if (equalityOperator.contains(operator)) {
				if (value instanceof List) {
					Predicate p = expression.in(((List<?>) value).toArray());
					return negateIfNeeded(p, operator);
				} else if (Collection.class.isAssignableFrom(expression.getJavaType())) {
					Predicate p = cb.literal(value).in(expression);
					return negateIfNeeded(p, operator);
				} else if (expression instanceof MapJoin) {
					Predicate p = cb.literal(value).in(((MapJoin) expression).value());
					return negateIfNeeded(p, operator);
				} else if (value == null) {
					if (operator.equals(FilterOperator.EQUAL)) {
						return cb.isNull(expression);
					} else {
						return cb.isNotNull(expression);
					}
				}
			}
			if (operator.hasReverseOrder()) {
				try {
					m = JpaPredicateBuilder.class.getDeclaredMethod(operator.getJpaQBuilderMethod(), operator.getJpaQBuilderParam2Class(), Expression.class);
				} catch (NoSuchMethodException e) {
					builder = cb;
					m = CriteriaBuilder.class.getDeclaredMethod(operator.getJpaQBuilderMethod(), operator.getJpaQBuilderParam2Class(), Expression.class);
				}
				return (Predicate) m.invoke(builder, value, expression);
			} else {
				// convert to String for LIKE operators
				if (expression.getJavaType() != String.class && likeOperator.contains(operator)) {
					expression = expression.as(String.class);
				}
				if (expression instanceof Predicate && expression.getJavaType() == Boolean.class && operator == FilterOperator.EQUAL) {
					if (value.equals(Boolean.TRUE)) {
						return (Predicate) expression;
					} else if (value.equals(Boolean.FALSE)) {
						return cb.not((Predicate) expression);
					}
				}

				try {
					m = JpaPredicateBuilder.class.getDeclaredMethod(operator.getJpaQBuilderMethod(), Expression.class, operator.getJpaQBuilderParam2Class());
				} catch (NoSuchMethodException e) {
					builder = cb;
					m = CriteriaBuilder.class.getDeclaredMethod(operator.getJpaQBuilderMethod(), Expression.class, operator.getJpaQBuilderParam2Class());
				}
				return (Predicate) m.invoke(builder, expression, value);
			}
		} catch (Exception e) {
			throw new RuntimeException("Exception while invoking method <" + operator.getJpaQBuilderMethod() + "> on + " + builder.getClass().getName() + ", " + e, e);
		}
	}

	protected abstract Expression<?> getAttribute(MetaAttributePath attrPath);

	public Predicate[] filterSpecListToPredicateArray(MetaDataObject rootMeta, From<?, ?> root, List<FilterSpec> rowFilters) {
		return filterSpecListToPredicateArray(rootMeta, root, rowFilters, false, null);
	}

	public Predicate[] filterSpecListToPredicateArray(MetaDataObject rootMeta, From<?, ?> root, List<FilterSpec> rowFilters, boolean forceEntityBased,
			JoinType defaultPredicateJoinType) {
		ArrayList<Predicate> predicateList = new ArrayList<Predicate>();

		for (FilterSpec rowFilter : rowFilters) {
			predicateList.add(filterSpecListToPredicate(rootMeta, root, rowFilter, forceEntityBased, defaultPredicateJoinType));
		}
		Predicate[] predicateArray = predicateList.toArray(new Predicate[predicateList.size()]);

		return predicateArray;
	}

	protected Predicate filterSpecListToPredicate(MetaDataObject rootMeta, From<?, ?> root, FilterSpec fs) {
		return filterSpecListToPredicate(rootMeta, root, fs, false, null);
	}

	protected Predicate filterSpecListToPredicate(MetaDataObject rootMeta, From<?, ?> root, FilterSpec fs, boolean forceEntityBased, JoinType defaultPredicateJoinType) {
		if ((fs.getOperator() == FilterOperator.EQUAL || fs.getOperator() == FilterOperator.NOT_EQUAL) && fs.getValue() instanceof Collection
				&& ((Collection<?>) fs.getValue()).size() > ORACLE_PARAM_LIMIT) {

			ArrayList<FilterSpec> specs = new ArrayList<FilterSpec>();
			List<?> list = new ArrayList<Object>((Collection<?>) fs.getValue());
			for (int i = 0; i < list.size(); i += ORACLE_PARAM_LIMIT) {
				int nextOffset = i + Math.min(list.size() - i, ORACLE_PARAM_LIMIT);
				List<?> batchList = list.subList(i, nextOffset);
				specs.add(new FilterSpec(fs.getAttributeName(), fs.getOperator(), batchList));
			}

			FilterSpec orSpec = FilterSpec.or(specs);
			return filterSpecListToPredicate(rootMeta, root, orSpec, forceEntityBased, defaultPredicateJoinType);
		} else {
			// and, or, etc.
			// KEEP IN SYNC with QueryExecutor.serializeSQLExpression(FilterSpec,
			// StringBuilder, int)
			if (fs.hasExpressions()) {
				if (fs.getOperator() == FilterOperator.NOT) {
					if (fs.getExpression().size() > 1) {
						return cb.not(cb.and(filterSpecListToPredicateArray(rootMeta, root, fs.getExpression(), forceEntityBased, defaultPredicateJoinType)));
					} else {
						FilterSpec expr = fs.getExpression().get(0);
						return cb.not(filterSpecListToPredicate(rootMeta, root, expr, forceEntityBased, defaultPredicateJoinType));
					}
				} else {
					FilterOperator operator = fs.getOperator();
					String methodName = operator.getJpaQBuilderMethod();
					Predicate[] predicates = filterSpecListToPredicateArray(rootMeta, root, fs.getExpression(), forceEntityBased, defaultPredicateJoinType);

					try {
						Method m = CriteriaBuilder.class.getDeclaredMethod(methodName, Predicate[].class);
						// Casting predicates to Object is required since Java
						// chooses varargs method instead
						return (Predicate) m.invoke(cb, (Object) predicates);
					} catch (InvocationTargetException e) {
						throw new RuntimeException("Exception while invoking method <" + methodName + "> on + " + CriteriaBuilder.class.getName() + ", " + e);
					} catch (Exception e) {
						throw new RuntimeException("Could not invoke method <" + methodName + "> on + " + CriteriaBuilder.class.getName() + ", " + e);
					}
				}
			}
			// equal, like etc.
			else {
				Object value = fs.getValue();
				if (value instanceof Set) {
					// HashSet not properly supported, convert to list
					Set<?> set = (Set<?>) value;
					value = new ArrayList<Object>(set);
				}

				MetaAttributePath path = getMetaObject(rootMeta, forceEntityBased).resolvePath(fs.getAttributeName(), true);
				path = enhanceAttributePath(path, value);
				Expression<?> attr = getAttribute(path);
				return buildPredicate(fs.getOperator(), attr, value);
			}
		}
	}

	public MetaAttributePath enhanceAttributePath(MetaAttributePath attrPath, Object value) {
		MetaAttribute attr = attrPath.getLast();

		MetaType valueType = attr.getType();
		if (valueType instanceof MetaMapType) {
			valueType = ((MetaMapType) valueType).getValueType();
		}

		boolean anyType = AnyTypeObject.class.isAssignableFrom(valueType.getImplementationClass());
		if (anyType) {
			// we have and AnyType and do need to select the proper attribute of the embeddable
			MetaAttribute anyAttr = AnyUtils.findAttribute((MetaDataObject) valueType, value);
			return attrPath.concat(anyAttr);
		} else {
			return attrPath;
		}
	}

	public static MetaDataObject getMetaObject(MetaDataObject meta, boolean forceEntityBased) {
		if (forceEntityBased && meta instanceof MetaProjection) {
			return ((MetaProjection) meta).getBaseType();
		} else {
			return meta;
		}
	}

}
