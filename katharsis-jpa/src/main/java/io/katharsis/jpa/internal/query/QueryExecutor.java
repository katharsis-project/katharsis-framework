package io.katharsis.jpa.internal.query;

import java.util.List;

import javax.persistence.criteria.CriteriaQuery;

public interface QueryExecutor<T> {

	public long getTotalRowCount();

	public T getUniqueResult(boolean nullable);

	public List<T> getResultList();

	public QueryExecutor<T> setWindow(int skip, int limit);

	public QueryExecutor<T> setCached(boolean cached);

	public CriteriaQuery<T> getQuery();

	public QueryExecutor<T> fetch(String... path);

	public Class<T> getEntityClass();
}
