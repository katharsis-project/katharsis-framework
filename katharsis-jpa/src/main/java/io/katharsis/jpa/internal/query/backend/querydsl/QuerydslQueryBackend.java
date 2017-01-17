package io.katharsis.jpa.internal.query.backend.querydsl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;
import javax.persistence.criteria.JoinType;

import com.google.common.collect.ImmutableList;
import com.querydsl.core.support.FetchableSubQueryBase;
import com.querydsl.core.types.CollectionExpression;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.OperationImpl;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.QTuple;
import com.querydsl.core.types.dsl.BeanPath;
import com.querydsl.core.types.dsl.CollectionExpressionBase;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.LiteralExpression;
import com.querydsl.core.types.dsl.MapExpressionBase;
import com.querydsl.core.types.dsl.MapPath;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAQueryBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import io.katharsis.jpa.internal.query.ComputedAttributeRegistryImpl;
import io.katharsis.jpa.internal.query.JoinRegistry;
import io.katharsis.jpa.internal.query.MetaComputedAttribute;
import io.katharsis.jpa.internal.query.QueryUtil;
import io.katharsis.jpa.internal.query.backend.JpaQueryBackend;
import io.katharsis.jpa.meta.MetaEntity;
import io.katharsis.jpa.query.querydsl.QuerydslExpressionFactory;
import io.katharsis.jpa.query.querydsl.QuerydslTranslationContext;
import io.katharsis.meta.model.MetaAttribute;
import io.katharsis.meta.model.MetaAttributePath;
import io.katharsis.meta.model.MetaKey;
import io.katharsis.queryspec.Direction;
import io.katharsis.queryspec.FilterOperator;
import io.katharsis.utils.PreconditionUtil;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class QuerydslQueryBackend<T>
		implements QuerydslTranslationContext<T>, JpaQueryBackend<Expression<?>, OrderSpecifier<?>, Predicate, Expression<?>> {

	private JoinRegistry<Expression<?>, Expression<?>> joinHelper;

	private Path<T> root;

	private EntityPath<?> parentFrom;

	private QuerydslQueryImpl<T> queryImpl;

	private JPAQueryBase querydslQuery;

	private List<OrderSpecifier<?>> orderList = new ArrayList<>();

	public QuerydslQueryBackend(QuerydslQueryImpl<T> queryImpl, Class<T> clazz, Class<?> parentEntityClass,
			MetaAttribute parentAttr, boolean addParentSelection) {
		this.queryImpl = queryImpl;

		JPAQueryFactory queryFactory = queryImpl.getQueryFactory();

		if (parentEntityClass != null) {
			parentFrom = QuerydslUtils.getEntityPath(parentEntityClass);
			root = QuerydslUtils.getEntityPath(clazz);

			Path joinPath = (Path) QuerydslUtils.get(parentFrom, parentAttr.getName());
			joinHelper = new JoinRegistry<>(this, queryImpl);

			joinHelper.putJoin(new MetaAttributePath(), root);

			if (addParentSelection) {
				Expression<Object> parentIdExpr = getParentIdExpression(parentAttr);
				querydslQuery = queryFactory.select(parentIdExpr, root);
			}
			else {
				querydslQuery = queryFactory.select(root);
			}

			querydslQuery = querydslQuery.from(parentFrom);
			if (joinPath instanceof CollectionExpression) {
				querydslQuery = querydslQuery.join((CollectionExpression) joinPath, root);
			}
			else {
				querydslQuery = querydslQuery.join((EntityPath) joinPath, root);
			}
		}
		else {
			root = QuerydslUtils.getEntityPath(clazz);
			joinHelper = new JoinRegistry<>(this, queryImpl);
			joinHelper.putJoin(new MetaAttributePath(), root);
			querydslQuery = queryFactory.select(root);
			querydslQuery = querydslQuery.from((EntityPath) root);
		}
	}

	private Expression<Object> getParentIdExpression(MetaAttribute parentAttr) {
		MetaEntity parentEntity = (MetaEntity) parentAttr.getParent();
		MetaKey primaryKey = parentEntity.getPrimaryKey();
		List<MetaAttribute> elements = primaryKey.getElements();
		PreconditionUtil.assertEquals("composite primary keys not supported yet", 1, elements.size());
		MetaAttribute primaryKeyAttr = elements.get(0);
		return QuerydslUtils.get(parentFrom, primaryKeyAttr.getName());
	}

	public JPAQuery<T> getQuery() {
		JPAQueryBase finalQuery = querydslQuery;
		for (OrderSpecifier<?> order : orderList) {
			finalQuery = (JPAQueryBase) finalQuery.orderBy(order);
		}
		return (JPAQuery<T>) finalQuery;
	}

	@Override
	public Expression<?> getAttribute(MetaAttributePath attrPath) {
		return joinHelper.getEntityAttribute(attrPath);
	}

	@Override
	public void addPredicate(Predicate predicate) {
		querydslQuery = (JPAQueryBase) querydslQuery.where(predicate);
	}

	@Override
	public Path<T> getRoot() {
		return root;
	}

	@Override
	public void setOrder(List<OrderSpecifier<?>> list) {
		this.orderList = list;
	}

	@Override
	public List<OrderSpecifier<?>> getOrderList() {
		return orderList;
	}

	@Override
	public OrderSpecifier<?> newSort(Expression<?> expr, Direction dir) {
		if (dir == Direction.ASC) {
			return new OrderSpecifier(Order.ASC, expr);
		}
		else {
			return new OrderSpecifier(Order.DESC, expr);
		}
	}

	@Override
	public void distinct() {
		querydslQuery = (JPAQueryBase) querydslQuery.distinct();
	}

	@Override
	public void addParentPredicate(MetaAttribute primaryKeyAttr) {
		List<?> parentIds = queryImpl.getParentIds();
		SimpleExpression<?> parentIdPath = (SimpleExpression<?>) QuerydslUtils.get(parentFrom, primaryKeyAttr.getName());
		addPredicate(parentIdPath.in((List) parentIds));
	}

	@Override
	public boolean hasManyRootsFetchesOrJoins() {
		return QuerydslUtils.hasManyRootsFetchesOrJoins((JPAQuery<?>) querydslQuery);
	}

	@Override
	public void addSelection(Expression<?> expression, String name) {
		Expression<?> selection = querydslQuery.getMetadata().getProjection();

		List<Expression<?>> newSelection = new ArrayList<>();
		if (selection != null) {
			if (selection instanceof QTuple) {
				newSelection.addAll(((QTuple) selection).getArgs());
			}
			else {
				newSelection.add(selection);
			}
		}
		newSelection.add(expression);
		querydslQuery = (JPAQuery) querydslQuery.select(newSelection.toArray(new Expression[newSelection.size()]));
	}

	@Override
	public Expression<?> getExpression(OrderSpecifier<?> order) {
		return order.getTarget();
	}

	@Override
	public boolean containsRelation(Expression<?> expression) {
		return QueryUtil.containsRelation(expression);
	}

	@Override
	public Predicate buildPredicate(FilterOperator operator, MetaAttributePath attrPath, Object value) {
		Expression<?> attr = getAttribute(attrPath);
		return buildPredicate(operator, attr, value);
	}

	public Predicate buildPredicate(FilterOperator operator, Expression<?> expressionObj, Object value) {
		Expression expression = expressionObj;

		expression = handleConversions(expression, operator);

		return handle(expression, operator, value);

	}

	private Predicate handle(Expression expression, FilterOperator operator, Object value) { // NOSONAR
		// checking multiple comparision implementations is a mess, created
		// https://github.com/querydsl/querydsl/issues/2028
		if (operator == FilterOperator.EQ || operator == FilterOperator.NEQ) {
			return handleEquals(expression, operator, value);
		}
		else if (operator == FilterOperator.LIKE) {
			return ((StringExpression) expression).lower().like(value.toString().toLowerCase());
		}
		else if (operator == FilterOperator.GT) {
			if (expression instanceof FetchableSubQueryBase) {
				return ((FetchableSubQueryBase) expression).gt((Number) value);
			}
			else if (expression instanceof NumberExpression) {
				return ((NumberExpression) expression).gt((Number) value);
			}
			else {
				return ((ComparableExpression) expression).gt((Comparable) value);
			}
		}
		else if (operator == FilterOperator.LT) {
			if (expression instanceof FetchableSubQueryBase) {
				return ((FetchableSubQueryBase) expression).lt((Number) value);
			}
			else if (expression instanceof NumberExpression) {
				return ((NumberExpression) expression).lt((Number) value);
			}
			else {
				return ((ComparableExpression) expression).lt((Comparable) value);
			}
		}
		else if (operator == FilterOperator.GE) {
			if (expression instanceof FetchableSubQueryBase) {
				return ((FetchableSubQueryBase) expression).goe((Number) value);
			}
			else if (expression instanceof NumberExpression) {
				return ((NumberExpression) expression).goe((Number) value);
			}
			else {
				return ((ComparableExpression) expression).goe((Comparable) value);
			}
		}
		else if (operator == FilterOperator.LE) {
			if (expression instanceof FetchableSubQueryBase) {
				return ((FetchableSubQueryBase) expression).loe((Number) value);
			}
			else if (expression instanceof NumberExpression) {
				return ((NumberExpression) expression).loe((Number) value);
			}
			else {
				return ((ComparableExpression) expression).loe((Comparable) value);
			}
		}
		else {
			throw new IllegalStateException("unexpected operator " + operator);
		}

	}

	private Predicate handleEquals(Expression<?> expression, FilterOperator operator, Object value) {
		if (value instanceof List) {
			Predicate p = ((SimpleExpression) expression).in((List) value);
			return negateIfNeeded(p, operator);
		}
		else if (Collection.class.isAssignableFrom(expression.getType())) {
			SimpleExpression simpleExpr = (SimpleExpression) expression;
			Predicate p = simpleExpr.in(value);
			return negateIfNeeded(p, operator);
		}
		else if (expression instanceof MapExpressionBase) {
			MapExpressionBase mapExpression = (MapExpressionBase) expression;
			Predicate p = mapExpression.containsValue(value);
			return negateIfNeeded(p, operator);
		}
		else if (value == null) {
			return negateIfNeeded(((SimpleExpression) expression).isNull(), operator);
		}
		return negateIfNeeded(((SimpleExpression) expression).eq(value), operator);
	}

	private Expression<?> handleConversions(Expression<?> expression, FilterOperator operator) {
		// convert to String for LIKE operators
		if (expression.getType() != String.class && (operator == FilterOperator.LIKE)) {
			return ((LiteralExpression) expression).stringValue();
		}
		else {
			return expression;
		}
	}

	@Override
	public Predicate and(List<Predicate> predicates) {
		if (predicates.size() == 1) {
			return predicates.get(0);
		}
		else {
			// only two elements for each operation supported, needs querydsl fix?
			Predicate result = predicates.get(0);
			for (int i = 1; i < predicates.size(); i++) {
				result = new BooleanPredicateOperation(Ops.AND, (ImmutableList) ImmutableList.of(result, predicates.get(i)));
			}
			return result;
		}
	}

	@Override
	public Predicate not(Predicate predicate) {
		return predicate.not();
	}

	@Override
	public Predicate or(List<Predicate> predicates) {
		if (predicates.size() == 1) {
			return predicates.get(0);
		}
		else {
			// only two elements for each operation supported, needs querydsl fix?
			Predicate result = predicates.get(0);
			for (int i = 1; i < predicates.size(); i++) {
				result = new BooleanPredicateOperation(Ops.OR, (ImmutableList) ImmutableList.of(result, predicates.get(i)));
			}
			return result;
		}
	}

	public final class BooleanPredicateOperation extends OperationImpl<Boolean> implements Predicate {

		private static final long serialVersionUID = -5371430939203772072L;

		@Nullable
		private transient volatile Predicate not;

		protected BooleanPredicateOperation(Ops ops, ImmutableList<Expression<?>> list) {
			super(Boolean.class, ops, list);
			if (list.isEmpty()) {
				throw new IllegalArgumentException("list cannot be empty");
			}
		}

		@Override
		public Predicate not() {
			if (not == null) {
				not = ExpressionUtils.predicate(Ops.NOT, this);
			}
			return not;
		}
	}

	private Predicate negateIfNeeded(Predicate p, FilterOperator fc) {
		if (fc.equals(FilterOperator.NEQ)) {
			return p.not();
		}
		return p;
	}

	@Override
	public Expression<?> joinMapValue(Expression<?> currentCriteriaPath, MetaAttribute pathElement, Object key) {
		MapPath mapPath = (MapPath) QuerydslUtils.get(currentCriteriaPath, pathElement.getName());
		return mapPath.get(key);
	}

	@Override
	public Class<?> getJavaElementType(Expression<?> expression) {
		if (expression instanceof CollectionExpressionBase) {
			return ((CollectionExpressionBase) expression).getElementType();
		}
		return expression.getType();
	}

	@Override
	public Expression<?> getAttribute(final Expression<?> expression, MetaAttribute pathElement) {
		if (pathElement instanceof MetaComputedAttribute) {
			ComputedAttributeRegistryImpl virtualAttrs = queryImpl.getComputedAttrs();
			QuerydslExpressionFactory expressionFactory = (QuerydslExpressionFactory) virtualAttrs
					.get((MetaComputedAttribute) pathElement);
			return expressionFactory.getExpression(expression, getQuery());
		}
		else {
			return QuerydslUtils.get(expression, pathElement.getName());
		}
	}

	@Override
	public Expression<?> joinSubType(Expression<?> expression, Class<?> entityClass) {
		BeanPath beanPath = (BeanPath) expression;
		Class<?> queryClass = QuerydslUtils.getQueryClass(entityClass);
		return beanPath.as(queryClass);
	}

	@Override
	public Expression<?> doJoin(MetaAttribute targetAttr, JoinType joinType, Expression<?> parent) {
		if (targetAttr instanceof MetaComputedAttribute) {

			MetaComputedAttribute computedAttr = (MetaComputedAttribute) targetAttr;
			QuerydslExpressionFactory expressionFactory = (QuerydslExpressionFactory<?>) queryImpl.getComputedAttrs()
					.get(computedAttr);

			return expressionFactory.getExpression(parent, getQuery());
		}
		else {
			Expression<Object> expression = QuerydslUtils.get(parent, targetAttr.getName());
			querydslQuery.getMetadata().addJoin(QuerydslUtils.convertJoinType(joinType), expression);
			return expression;
		}
	}

	@Override
	public JPAQueryFactory getQueryFactory() {
		return queryImpl.getQueryFactory();
	}

	@Override
	public EntityPath getParentRoot() {
		return parentFrom;
	}

	@Override
	public <E> EntityPath<E> getJoin(MetaAttributePath path) {
		return (EntityPath<E>) joinHelper.getOrCreateJoin(path);
	}

	@Override
	public <U> QuerydslTranslationContext<U> castFor(Class<U> type) {
		return (QuerydslTranslationContext<U>) this;
	}
}
