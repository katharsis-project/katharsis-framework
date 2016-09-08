package io.katharsis.jpa.query.querydsl;

import java.util.List;

import io.katharsis.jpa.query.JpaQueryExecutor;

public interface QuerydslExecutor<T> extends JpaQueryExecutor<T> {

	@Override
	public List<QuerydslTuple> getResultTuples();
}
