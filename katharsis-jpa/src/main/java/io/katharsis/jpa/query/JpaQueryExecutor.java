package io.katharsis.jpa.query;

import java.util.List;

public interface JpaQueryExecutor<T> {

	/**
	 * @return Count the number of objects returned without any paging applied.
	 */
	public long getTotalRowCount();

	public T getUniqueResult(boolean nullable);

	public List<T> getResultList();

	public JpaQueryExecutor<T> setLimit(int limit);

	public JpaQueryExecutor<T> setOffset(int offset);

	public JpaQueryExecutor<T> setWindow(int offset, int limit);

	public JpaQueryExecutor<T> setCached(boolean cached);

	public JpaQueryExecutor<T> fetch(List<String> attrPath);

	public Class<T> getEntityClass();

	public <U extends Tuple> List<U> getResultTuples();
}
