package io.katharsis.jpa.internal.query.backend.criteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaAttributePath;
import io.katharsis.jpa.internal.query.JoinRegistry;
import io.katharsis.jpa.internal.query.MetaVirtualAttribute;
import io.katharsis.jpa.internal.query.QueryUtil;
import io.katharsis.jpa.internal.query.backend.JpaQueryBackend;
import io.katharsis.jpa.query.criteria.JpaCriteriaExpressionFactory;
import io.katharsis.queryspec.Direction;
import io.katharsis.queryspec.FilterOperator;

public class JpaCriteriaQueryBackend<T> implements JpaQueryBackend<From<?, ?>, Order, Predicate, Expression<?>> {

	private CriteriaQuery<T> criteriaQuery;

	private JoinRegistry<From<?, ?>, Expression<?>> joinHelper;

	protected CriteriaBuilder cb;

	private From<T, T> root;

	private Root<?> parentFrom;

	private JpaCriteriaQueryImpl<T> queryImpl;

	@SuppressWarnings("unchecked")
	public JpaCriteriaQueryBackend(JpaCriteriaQueryImpl<T> query, EntityManager em, Class<T> clazz, Class<?> parentEntityClass,
			MetaAttribute parentAttr) {
		this.queryImpl = query;

		cb = em.getCriteriaBuilder();
		criteriaQuery = (CriteriaQuery<T>) cb.createQuery();

		if (parentEntityClass != null) {
			parentFrom = criteriaQuery.from(parentEntityClass);
			root = parentFrom.join(parentAttr.getName());
			joinHelper = new JoinRegistry<>(this, query);
			joinHelper.putJoin(new MetaAttributePath(), root);
			criteriaQuery.select(root);
		}
		else {
			root = criteriaQuery.from(clazz);
			joinHelper = new JoinRegistry<>(this, query);
			joinHelper.putJoin(new MetaAttributePath(), root);
			criteriaQuery.select(root);
		}
	}

	@Override
	public Expression<?> getAttribute(MetaAttributePath attrPath) {
		return joinHelper.getEntityAttribute(attrPath);
	}

	@Override
	public void addPredicate(Predicate predicate) {
		Predicate restriction = criteriaQuery.getRestriction();
		if (restriction != null) {
			criteriaQuery.where(restriction, predicate);
		}
		else {
			criteriaQuery.where(predicate);
		}
	}

	@Override
	public From<?, ?> getRoot() {
		return root;
	}

	@Override
	public void setOrder(List<Order> list) {
		criteriaQuery.orderBy(list);
	}

	@Override
	public List<Order> getOrderList() {
		return new ArrayList<>(criteriaQuery.getOrderList());
	}

	@Override
	public Order newSort(Expression<?> expr, Direction dir) {
		if (dir == Direction.ASC) {
			return cb.asc(expr);
		}
		else {
			return cb.desc(expr);
		}
	}

	public CriteriaBuilder getCriteriaBuilder() {
		return cb;
	}

	@Override
	public void distinct() {
		criteriaQuery.distinct(true);
	}

	public CriteriaQuery<T> getCriteriaQuery() {
		return criteriaQuery;
	}

	@Override
	public void addParentPredicate(MetaAttribute primaryKeyAttr) {
		List<?> parentIds = queryImpl.getParentIds();
		Path<Object> parentIdPath = parentFrom.get(primaryKeyAttr.getName());
		addPredicate(parentIdPath.in(new ArrayList<Object>(parentIds)));
	}

	@Override
	public boolean hasManyRootsFetchesOrJoins() {
		return QueryUtil.hasManyRootsFetchesOrJoins(criteriaQuery);
	}

	@Override
	public void addSelection(Expression<?> expression, String name) {
		Selection<?> selection = criteriaQuery.getSelection();

		List<Selection<?>> newSelection = new ArrayList<>();
		if (selection != null) {
			if (selection.isCompoundSelection()) {
				newSelection.addAll(selection.getCompoundSelectionItems());
			}
			else {
				newSelection.add(selection);
			}
		}
		newSelection.add(expression);
		criteriaQuery.multiselect(newSelection);
	}

	@Override
	public Expression<?> getExpression(Order order) {
		return order.getExpression();
	}

	@Override
	public boolean containsRelation(Expression<?> expression) {
		return QueryUtil.containsRelation(expression);
	}

	public Predicate ilike(Expression<String> expr, String val) {
		return cb.like(cb.lower(expr), val.toLowerCase());
	}

	private Predicate negateIfNeeded(Predicate p, FilterOperator fc) {
		if (fc.equals(FilterOperator.NEQ))
			return cb.not(p);
		return p;
	}

	@Override
	public Predicate buildPredicate(FilterOperator operator, MetaAttributePath attrPath, Object value) {
		Expression<?> attr = getAttribute(attrPath);
		return buildPredicate(operator, attr, value);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Predicate buildPredicate(FilterOperator operator, Expression<?> expressionObj, Object value) {
		Expression expression = expressionObj;

		expression = handleConversions(expression, operator);

		if (expression instanceof Predicate && expression.getJavaType() == Boolean.class && operator == FilterOperator.EQ) {
			return handleBoolean(expression, value);
		}

		return handle(expression, operator, value);

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Predicate handleBoolean(Expression expression, Object value) {
		if (value.equals(Boolean.TRUE)) {
			return (Predicate) expression;
		}
		else {
			return cb.not(expression);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Predicate handle(Expression expression, FilterOperator operator, Object value) {
		if (operator == FilterOperator.EQ || operator == FilterOperator.NEQ) {
			return handleEquals(expression, operator, value);
		}
		else if (operator ==FilterOperator.LIKE) {
			return ilike(expression, value.toString());
		}
		else if (operator == FilterOperator.GT) {
			return cb.greaterThan(expression, (Comparable) value);
		}
		else if (operator == FilterOperator.LT) {
			return cb.lessThan(expression, (Comparable) value);
		}
		else if (operator == FilterOperator.GE) {
			return cb.greaterThanOrEqualTo(expression, (Comparable) value);
		}
		else if (operator == FilterOperator.LE) {
			return cb.lessThanOrEqualTo(expression, (Comparable) value);
		}
		else {
			throw new IllegalStateException("unexpected operator " + operator);
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Predicate handleEquals(Expression<?> expression, FilterOperator operator, Object value) {
		if (value instanceof List) {
			Predicate p = expression.in(((List<?>) value).toArray());
			return negateIfNeeded(p, operator);
		}
		else if (Collection.class.isAssignableFrom(expression.getJavaType())) {
			Predicate p = cb.literal(value).in(expression);
			return negateIfNeeded(p, operator);
		}
		else if (expression instanceof MapJoin) {
			Predicate p = cb.literal(value).in(((MapJoin) expression).value());
			return negateIfNeeded(p, operator);
		}
		else if (value == null) {
			return negateIfNeeded(cb.isNull(expression), operator);
		}
		return negateIfNeeded(cb.equal(expression, value), operator);
	}

	private Expression<?> handleConversions(Expression<?> expression, FilterOperator operator) {
		// convert to String for LIKE operators
		if (expression.getJavaType() != String.class && (operator == FilterOperator.LIKE)) {
			return expression.as(String.class);
		}
		else {
			return expression;
		}
	}

	@Override
	public Predicate and(List<Predicate> predicates) {
		return cb.and(predicates.toArray(new Predicate[predicates.size()]));
	}

	@Override
	public Predicate not(Predicate predicate) {
		return cb.not(predicate);
	}

	@Override
	public Predicate or(List<Predicate> predicates) {
		return cb.or(predicates.toArray(new Predicate[predicates.size()]));
	}

	@Override
	public Expression<?> joinMapValue(Expression<?> currentCriteriaPath, MetaAttribute pathElement, Object key) {
		MapJoin<Object, Object, Object> mapJoin = ((From<?, ?>) currentCriteriaPath).joinMap(pathElement.getName(),
				JoinType.LEFT);
		Predicate mapJoinCondition = cb.equal(mapJoin.key(), key);
		Predicate nullCondition = cb.isNull(mapJoin.key());
		addPredicate(cb.or(mapJoinCondition, nullCondition));
		return mapJoin;
	}

	@Override
	public Expression<?> joinMapValues(Expression<?> currentCriteriaPath, MetaAttribute mapPathElement) {
		MapJoin<Object, Object, Object> mapJoin = ((From<?, ?>) currentCriteriaPath).joinMap(mapPathElement.getName(),
				JoinType.LEFT);
		return mapJoin.value();
	}

	@Override
	public Expression<?> joinMapKey(Expression<?> currentCriteriaPath, MetaAttribute mapPathElement) {
		MapJoin<Object, Object, Object> mapJoin = ((From<?, ?>) currentCriteriaPath).joinMap(mapPathElement.getName(),
				JoinType.LEFT);
		return mapJoin.key();
	}

	@Override
	public Class<?> getJavaElementType(Expression<?> currentCriteriaPath) {
		return currentCriteriaPath.getJavaType();
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Expression<?> getAttribute(Expression<?> currentCriteriaPath, MetaAttribute pathElement) {
		if (pathElement instanceof MetaVirtualAttribute) {
			MetaVirtualAttribute projAttr = (MetaVirtualAttribute) pathElement;
			JpaCriteriaExpressionFactory expressionFactory = (JpaCriteriaExpressionFactory<?>) queryImpl.getVirtualAttrs()
					.get(projAttr);

			From from = (From) currentCriteriaPath;
			return expressionFactory.getExpression(from, getCriteriaQuery());
		}
		else {
			return ((Path<?>) currentCriteriaPath).get(pathElement.getName());
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Expression<?> joinSubType(Expression<?> currentCriteriaPath, Class<?> entityType) {
		return cb.treat((Path) currentCriteriaPath, (Class) entityType);
	}

	@SuppressWarnings("unchecked")
	@Override
	public From<?, ?> doJoin(MetaAttribute targetAttr, JoinType joinType, From<?, ?> parent) {
		if (targetAttr instanceof MetaVirtualAttribute) {
			MetaVirtualAttribute projAttr = (MetaVirtualAttribute) targetAttr;
			@SuppressWarnings("rawtypes")
			JpaCriteriaExpressionFactory expressionFactory = (JpaCriteriaExpressionFactory<?>) queryImpl.getVirtualAttrs()
					.get(projAttr);

			return (From<?, ?>) expressionFactory.getExpression(parent, getCriteriaQuery());
		}
		else {
			return parent.join(targetAttr.getName(), joinType);
		}
	}

}