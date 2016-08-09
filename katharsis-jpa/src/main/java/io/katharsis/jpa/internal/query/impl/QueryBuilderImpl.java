package io.katharsis.jpa.internal.query.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaAttributePath;
import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaKey;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.internal.query.FilterOperator;
import io.katharsis.jpa.internal.query.FilterSpec;
import io.katharsis.jpa.internal.query.OrderSpec;
import io.katharsis.jpa.internal.query.OrderSpec.Direction;
import io.katharsis.jpa.internal.query.QueryBuilder;
import io.katharsis.utils.StringUtils;

public class QueryBuilderImpl<T> implements QueryBuilder<T> {

	private static final String SORT_COLUMN_ALIAS_PREFIX = "__sort";

	protected final EntityManager em;
	protected final MetaDataObject meta;
	protected final Class<T> clazz;

	private JoinType defaultJoinType = JoinType.INNER;
	protected final Map<MetaAttributePath, JoinType> joinTypes = new HashMap<MetaAttributePath, JoinType>();

	private ArrayList<FilterSpec> filterSpecs = new ArrayList<FilterSpec>();
	private ArrayList<OrderSpec> orderSpecs = new ArrayList<OrderSpec>();

	private boolean autoDistinct = true;
	private boolean autoGroupBy = false;
	private boolean distinct = false;
	private boolean ensureTotalOrder = true;

	private VirtualAttributeRegistry virtualAttrs = new VirtualAttributeRegistry();

	private Class<?> parentEntityClass;
	private List<?> parentIds;
	private MetaAttribute parentAttr;

	protected QueryBuilderImpl(EntityManager em, Class<T> clazz) {
		this.em = em;
		this.clazz = clazz;
		this.meta = MetaLookup.INSTANCE.getMeta(clazz).asDataObject();
	}

	public QueryBuilderImpl(EntityManager em, Class<?> entityClass, String attrName, List<?> entityIds) {
		this.em = em;

		MetaDataObject parentMeta = MetaLookup.INSTANCE.getMeta(entityClass).asDataObject();
		MetaAttribute attrMeta = parentMeta.getAttribute(attrName);
		this.meta = attrMeta.getType().asEntity();
		this.clazz = (Class<T>) meta.getImplementationClass();

		this.parentEntityClass = entityClass;
		this.parentAttr = attrMeta;
		this.parentIds = entityIds;
	}

	@Override
	public QueryBuilder<T> setEnsureTotalOrder(boolean ensureTotalOrder) {
		this.ensureTotalOrder = ensureTotalOrder;
		return this;
	}

	@Override
	public QueryBuilder<T> addFilter(FilterSpec filters) {
		this.filterSpecs.add(filters);
		return this;
	}

	@Override
	public QueryBuilder<T> addOrderBy(Direction dir, String... path) {
		this.orderSpecs
				.add(new OrderSpec(StringUtils.join(MetaAttributePath.PATH_SEPARATOR, Arrays.asList(path)), dir));
		return this;
	}

	@Override
	public QueryBuilder<T> addOrder(OrderSpec order) {
		this.orderSpecs.add(order);
		return this;
	}

	@Override
	public QueryBuilder<T> setDefaultJoinType(JoinType joinType) {
		this.defaultJoinType = joinType;
		return this;
	}

	@Override
	public QueryBuilder<T> setJoinType(JoinType joinType, String... path) {
		joinTypes.put(meta.resolvePath(StringUtils.join(MetaAttributePath.PATH_SEPARATOR, Arrays.asList(path))),
				joinType);
		return this;
	}

	@Override
	public QueryBuilder<T> setAutoGroupBy(boolean autoGroupBy) {
		this.autoGroupBy = autoGroupBy;
		return this;
	}

	@Override
	public QueryBuilder<T> setDistinct(boolean distinct) {
		this.autoDistinct = false;
		this.distinct = distinct;
		return this;
	}

	@Override
	public QueryBuilder<T> addFilter(String attrPath, FilterOperator filterOperator, Object value) {
		addFilter(new FilterSpec(attrPath, filterOperator, value));
		return this;
	}

	protected static class QueryBuilderContext<T> {

		public CriteriaQuery<T> criteriaQuery;
		public CriteriaJoinRegistry joinHelper;
		public CriteriaBuilder cb;
		public From<T, T> root;
		public Root<?> parentFrom;

		public QueryBuilderContext() {
		}

		public void addPredicate(Predicate predicate) {
			Predicate restriction = criteriaQuery.getRestriction();
			if (restriction != null) {
				criteriaQuery.where(restriction, predicate);
			} else {
				criteriaQuery.where(predicate);
			}
		}
	}

	@Override
	public CriteriaQuery<T> buildQuery() {
		return buildExecutor().getQuery();
	}

	@Override
	public QueryExecutorImpl<T> buildExecutor() {
		QueryBuilderContext<T> ctx = new QueryBuilderContext<T>();

		ctx.cb = em.getCriteriaBuilder();
		ctx.criteriaQuery = (CriteriaQuery<T>) ctx.cb.createQuery();

		if (parentEntityClass != null) {
			ctx.parentFrom = ctx.criteriaQuery.from(parentEntityClass);
			ctx.root = ctx.parentFrom.join(parentAttr.getName());
			ctx.joinHelper = new CriteriaJoinRegistry(ctx, this);
			ctx.joinHelper.putJoin(new MetaAttributePath(), ctx.root);
			ctx.criteriaQuery.select(ctx.root);
		} else {
			ctx.root = ctx.criteriaQuery.from(clazz);
			ctx.joinHelper = new CriteriaJoinRegistry(ctx, this);
			ctx.joinHelper.putJoin(new MetaAttributePath(), ctx.root);
			ctx.criteriaQuery.select(ctx.root);
		}

		applySelectExpr(ctx);
		applyFilterSpec(ctx);
		applyOrderSpec(ctx);
		applyAutoGoupBy(ctx);
		int numAutoSelections = applyDistinct(ctx.criteriaQuery);

		QueryExecutorImpl<T> executor = new QueryExecutorImpl<T>(em, meta, ctx.criteriaQuery, numAutoSelections);
		return executor;
	}

	private void applyOrderSpec(final QueryBuilderContext<T> ctx) {
		JpaOrderBuilder<T> orderBuilder = new JpaOrderBuilder<T>(this, ctx) {
			@Override
			protected Expression<?> getAttribute(MetaAttributePath attrPath) {
				return QueryBuilderImpl.this.getAttribute(ctx, attrPath);
			}

		};
		orderBuilder.applyOrderSpec();
	}

	protected void applySelectExpr(QueryBuilderContext<T> ctx) {

	}

	protected void applyAutoGoupBy(QueryBuilderContext<T> ctx) {
		if (autoGroupBy && (ctx.criteriaQuery.getGroupList() == null || ctx.criteriaQuery.getGroupList().isEmpty())) {
			ctx.criteriaQuery.groupBy(ctx.root);
		}
	}

	protected void applyFilterSpec(final QueryBuilderContext<T> ctx) {
		JpaPredicateBuilder predicateBuilder = new JpaPredicateBuilder(ctx.cb) {
			@Override
			protected Expression<?> getAttribute(MetaAttributePath attrPath) throws IllegalArgumentException {
				return QueryBuilderImpl.this.getAttribute(ctx, attrPath);
			}
		};

		Predicate[] predicates = predicateBuilder.filterSpecListToPredicateArray(meta, ctx.root, filterSpecs);
		if (predicates != null) {
			ctx.addPredicate(ctx.cb.and(predicates));
		}

		if (parentIds != null) {
			MetaEntity parentEntity = parentAttr.getParent().asEntity();
			MetaKey primaryKey = parentEntity.getPrimaryKey();
			MetaAttribute primaryKeyAttr = primaryKey.getUniqueElement();
			Path<Object> parentIdPath = ctx.parentFrom.get(primaryKeyAttr.getName());
			ctx.addPredicate(parentIdPath.in(new ArrayList<Object>(parentIds)));
		}
	}

	protected Expression<?> getAttribute(QueryBuilderContext<T> ctx, MetaAttributePath attrPath) {
		return ctx.joinHelper.getEntityAttribute(attrPath);
	}

	/**
	 * Returns true if distinct is set manually or the "auto distinct" mode is
	 * enabled and the user performs a join or fetch on a many assocation. In
	 * this case, attributes from referenced entities included in the sort
	 * clause are also added to the select clause.
	 */
	private int applyDistinct(CriteriaQuery<?> criteriaQuery) {
		int numAutoSelections = 0;
		if (autoDistinct) {
			// distinct for many join/fetches or manual
			// in case of ViewQuery we may not need this here, but we need to do
			// the selection of order-by columns below
			boolean distinct = autoDistinct && !autoGroupBy && QueryUtil.hasManyRootsFetchesOrJoins(criteriaQuery);
			criteriaQuery.distinct(distinct);
			if (distinct) {
				// we also need to select sorted attributes (for references)
				int prefixIndex = 0;
				ArrayList<Order> newOrderList = new ArrayList<Order>();
				for (Order order : criteriaQuery.getOrderList()) {
					Expression<?> expr = order.getExpression();
					if (QueryUtil.containsRelation(expr)) {
						addSelection(criteriaQuery, expr, SORT_COLUMN_ALIAS_PREFIX + prefixIndex++);
						numAutoSelections++;
					}
					newOrderList.add(order);
				}
				criteriaQuery.orderBy(newOrderList);
			}
		} else {
			criteriaQuery.distinct(distinct);
		}
		return numAutoSelections;
	}

	// @SuppressWarnings({ "unchecked", "rawtypes" })
	// private void removeDistinct(CriteriaQuery<?> criteriaQuery) {
	// if (isAutoDistinct() &&
	// criteriaQuery.getSelection().isCompoundSelection()) {
	// ArrayList<Selection<?>> newSelection = new ArrayList<Selection<?>>();
	// for (Selection<?> selection :
	// criteriaQuery.getSelection().getCompoundSelectionItems()) {
	// String alias = selection.getAlias();
	// if (alias == null ||
	// !selection.getAlias().startsWith(SORT_COLUMN_ALIAS_PREFIX)) {
	// newSelection.add(selection);
	// }
	// }
	// if (newSelection.size() == 1)
	// criteriaQuery.select((Selection) newSelection.get(0));
	// else
	// criteriaQuery.multiselect(newSelection);
	// }
	// }

	protected ArrayList<OrderSpec> getOrderSpecs() {
		return orderSpecs;
	}

	protected boolean getEnsureTotalOrder() {
		return ensureTotalOrder;
	}

	// /**
	// * Add a new expression to query's select clause
	// */
	// @Override
	// public void addSelection(Selection<?> s, String name) {
	// addSelection(name, s);
	// }
	//
	// @Override
	// protected void addSelection(String name, Selection<?> s) {
	// addSelectionImpl(criteriaQuery, s, name);
	// }

	protected void addSelection(CriteriaQuery<?> criteriaQuery, Selection<?> s, String name) {
		Selection<?> selection = criteriaQuery.getSelection();

		List<Selection<?>> newSelection = new ArrayList<Selection<?>>();
		if (selection != null) {
			if (selection.isCompoundSelection()) {
				newSelection.addAll(selection.getCompoundSelectionItems());
			} else {
				newSelection.add(selection);
			}
		}
		newSelection.add(s);
		criteriaQuery.multiselect(newSelection);
	}

	protected JoinType getJoinType(MetaAttributePath path) {
		JoinType joinType = joinTypes.get(path);
		if (joinType == null)
			joinType = defaultJoinType;
		return joinType;
	}

	protected VirtualAttributeRegistry getVirtualAttrs() {
		return virtualAttrs;
	}

	protected MetaDataObject getMeta() {
		return meta;
	}

	@Override
	public Class<T> getEntityClass() {
		return clazz;
	}

	//
	// /*
	// * Convert the PCY OrderSpec into JPA Expression
	// *
	// *
	// http://city81.blogspot.ch/2011/01/criteriabuilder-and-dynamic-queries-in.html
	// * http://www.altuure.com/2010/09/23/jpa-criteria-api-by-samples-part-i/
	// */
	//
	// public Expression<?> getAttribute(String... attrPath) {
	// return getAttribute(StringUtils.join(attrPath,
	// MetaAttributePath.PATH_SEPARATOR));
	// }
	//
	// public final Expression<?> getAttribute(String attrPath) throws
	// IllegalArgumentException {
	// return getAttribute(attrPath, (JoinType) null);
	// }
	//
	// // protected abstract LinkedList<? extends IMetaAttribute>
	// resolveAttributePath(String attrPath);
	//
	// public Expression<?> getAttribute(JoinType joinType, String... attrPath)
	// {
	// return getAttribute(StringUtils.join(attrPath,
	// MetaAttributePath.PATH_SEPARATOR), joinType);
	// }
	//
	// public final Expression<?> getAttribute(String attributeName, JoinType
	// joinType) throws IllegalArgumentException
	// {
	// return getAttribute(attributeName, joinType, false);
	// }
	//
	// // protected abstract Expression<?> getAttribute(String attributeName,
	// JoinType joinType, boolean
	// forceEntityBased)
	// // throws IllegalArgumentException;

	// public From<?, ?> getJoin(String attributePath) {
	// return getJoin(attributePath, null);
	// }
	//
	// public abstract From<?, ?> getJoin(String attrPath, JoinType joinType);

}
