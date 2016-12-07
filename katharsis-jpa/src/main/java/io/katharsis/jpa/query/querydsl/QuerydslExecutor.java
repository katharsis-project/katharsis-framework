package io.katharsis.jpa.query.querydsl;

import java.util.List;

import com.querydsl.jpa.impl.JPAQuery;

import io.katharsis.jpa.query.JpaQueryExecutor;

public interface QuerydslExecutor<T> extends JpaQueryExecutor<T> {

	@Override
	public List<QuerydslTuple> getResultTuples();

	public void setQuery(JPAQuery<T> query);

	public JPAQuery<T> getQuery();
}
