package io.katharsis.jpa;

import io.katharsis.jpa.internal.query.QueryBuilder;
import io.katharsis.jpa.internal.query.QueryExecutor;
import io.katharsis.queryParams.QueryParams;

public interface QueryParamsProcessor {

	public <T> void prepareExecution(QueryExecutor<T> executor, QueryParams queryParams);

	public <T> void prepareQuery(QueryBuilder<T> builder, QueryParams queryParams);

}
