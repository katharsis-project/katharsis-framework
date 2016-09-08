package io.katharsis.jpa.query.criteria;

import java.util.List;

import javax.persistence.Tuple;

import io.katharsis.jpa.query.JpaQueryExecutor;

public interface JpaCriteriaQueryExecutor<T> extends JpaQueryExecutor<T> {

	/**
	 * @return tuple when doing a custom selection.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Tuple> getResultTuples();

}
