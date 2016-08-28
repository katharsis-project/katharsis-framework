package io.katharsis.repository.base.query;

import io.katharsis.queryParams.QueryParams;

public interface QuerySpecParser {

	public QuerySpec fromParams(QueryParams params);

	public QueryParams toParams(QuerySpec spec);

}
