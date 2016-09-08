package io.katharsis.jpa.query;

import java.util.List;

import javax.persistence.criteria.JoinType;

import io.katharsis.queryspec.Direction;
import io.katharsis.queryspec.FilterOperator;
import io.katharsis.queryspec.FilterSpec;
import io.katharsis.queryspec.SortSpec;

public interface JpaQuery<T> {

	public JpaQuery<T> setEnsureTotalOrder(boolean ensureTotalOrder);

	public JpaQuery<T> addFilter(FilterSpec filters);

	public JpaQuery<T> addSortBy(List<String> path, Direction dir);

	public JpaQuery<T> addSortBy(SortSpec order);

	public JpaQuery<T> setDefaultJoinType(JoinType joinType);

	public JpaQuery<T> setJoinType(List<String> path, JoinType joinType);

	public JpaQuery<T> setAutoGroupBy(boolean autoGroupBy);

	public JpaQuery<T> setDistinct(boolean distinct);

	public JpaQuery<T> addFilter(List<String> attrPath, FilterOperator operator, Object value);

	public JpaQuery<T> addFilter(String attrPath, FilterOperator operator, Object value);

	public JpaQueryExecutor<T> buildExecutor();

	public Class<T> getEntityClass();

	public void addSelection(List<String> path);

}
