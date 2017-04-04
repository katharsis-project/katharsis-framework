package io.katharsis.legacy.internal;

import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.legacy.queryParams.params.IncludedFieldsParams;
import io.katharsis.legacy.queryParams.params.IncludedRelationsParams;
import io.katharsis.legacy.queryParams.params.TypedParams;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.repository.request.QueryAdapter;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.ResourceRegistry;

public class QueryParamsAdapter implements QueryAdapter {

	private QueryParams queryParams;

	private ResourceInformation resourceInformation;

	private ResourceRegistry resourceRegistry;

	public QueryParamsAdapter(ResourceInformation resourceInformation, QueryParams queryParams, ResourceRegistry resourceRegistry) {
		this.queryParams = queryParams;
		this.resourceInformation = resourceInformation;
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
	public ResourceInformation getResourceInformation() {
		if (resourceInformation == null) {
			throw new IllegalStateException("resourceInformation not set");
		}
		return resourceInformation;
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
