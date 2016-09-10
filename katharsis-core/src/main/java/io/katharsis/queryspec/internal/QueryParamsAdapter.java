package io.katharsis.queryspec.internal;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.params.IncludedFieldsParams;
import io.katharsis.queryParams.params.IncludedRelationsParams;
import io.katharsis.queryParams.params.TypedParams;

public class QueryParamsAdapter implements QueryAdapter {

	private QueryParams queryParams;

	public QueryParamsAdapter(QueryParams queryParams) {
		this.queryParams = queryParams;
	}

	public QueryParams getQueryParams() {
		return queryParams;
	}

	@Override
	public boolean hasIncludedRelations() {
		return queryParams.getIncludedRelations() != null && !queryParams.getIncludedRelations().getParams().isEmpty();
	}

	@Override
	public TypedParams<IncludedRelationsParams> getIncludedRelations() {
		return queryParams.getIncludedRelations();
	}

	@Override
	public TypedParams<IncludedFieldsParams> getIncludedFields() {
		return queryParams.getIncludedFields();
	}
}
