package io.katharsis.jpa.query.criteria;

import io.katharsis.jpa.query.JpaQuery;

public interface JpaCriteriaQuery<T> extends JpaQuery<T> {

	@Override
	public JpaCriteriaQueryExecutor<T> buildExecutor();
}
