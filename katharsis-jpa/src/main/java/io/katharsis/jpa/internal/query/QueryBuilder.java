package io.katharsis.jpa.internal.query;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;

import io.katharsis.jpa.internal.query.OrderSpec.Direction;

public interface QueryBuilder<T> {

	public QueryBuilder<T> setEnsureTotalOrder(boolean ensureTotalOrder);

	public QueryBuilder<T> addFilter(FilterSpec filters);

	public QueryBuilder<T> addOrderBy(Direction dir, String... path);

	public QueryBuilder<T> addOrder(OrderSpec order);

	public QueryBuilder<T> setDefaultJoinType(JoinType joinType);

	public QueryBuilder<T> setJoinType(JoinType joinType, String... path);

	public QueryBuilder<T> setAutoGroupBy(boolean autoGroupBy);

	public QueryBuilder<T> setDistinct(boolean distinct);

	// FIXME remo signature
	public QueryBuilder<T> addFilter(String attrPath, FilterOperator operator, Object value);

	public CriteriaQuery<T> buildQuery();

	public QueryExecutor<T> buildExecutor();

	public Class<T> getEntityClass();
}
