package io.katharsis.jpa;

import io.katharsis.jpa.query.JpaQuery;
import io.katharsis.jpa.query.JpaQueryExecutor;
import io.katharsis.legacy.queryParams.QueryParams;

public interface QueryParamsProcessor {

	public <T> void prepareExecution(JpaQueryExecutor<T> executor, QueryParams queryParams);

	public <T> void prepareQuery(JpaQuery<T> builder, QueryParams queryParams);

}
