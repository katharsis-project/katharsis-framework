package io.katharsis.queryspec.internal;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.params.IncludedFieldsParams;
import io.katharsis.queryParams.params.IncludedRelationsParams;
import io.katharsis.queryParams.params.TypedParams;
import io.katharsis.resource.registry.ResourceRegistry;

public class QueryParamsAdapter implements QueryAdapter {

	private QueryParams queryParams;

	private Class<?> resourceClass;

	private ResourceRegistry resourceRegistry;

	public QueryParamsAdapter(Class<?> resourceClass, QueryParams queryParams, ResourceRegistry resourceRegistry) {
		this.queryParams = queryParams;
		this.resourceClass = resourceClass;
		this.resourceRegistry = resourceRegistry;
	}

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

	@Override
	public Class<?> getResourceClass() {
		if (resourceClass == null) {
			throw new IllegalStateException("resourceClass not set");
		}
		return resourceClass;
	}

	public ResourceRegistry getResourceRegistry() {
		if (resourceRegistry == null) {
			throw new IllegalStateException("resourceRegistry not set");
		}
		return resourceRegistry;
	}

	@Override
	public Long getLimit() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getOffset() {
		throw new UnsupportedOperationException();
	}

	@Override
	public QueryAdapter duplicate() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLimit(Long limit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setOffset(long offset) {
		throw new UnsupportedOperationException();
	}
}
