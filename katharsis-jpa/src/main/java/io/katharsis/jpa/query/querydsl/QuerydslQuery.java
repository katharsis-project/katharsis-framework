package io.katharsis.jpa.query.querydsl;

import io.katharsis.jpa.query.JpaQuery;

public interface QuerydslQuery<T> extends JpaQuery<T> {

	@Override
	public QuerydslExecutor<T> buildExecutor();
}
