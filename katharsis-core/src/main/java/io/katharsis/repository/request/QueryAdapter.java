package io.katharsis.repository.request;

import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.legacy.queryParams.params.IncludedFieldsParams;
import io.katharsis.legacy.queryParams.params.IncludedRelationsParams;
import io.katharsis.legacy.queryParams.params.TypedParams;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.resource.information.ResourceInformation;

public interface QueryAdapter {

	boolean hasIncludedRelations();

	TypedParams<IncludedRelationsParams> getIncludedRelations();

	TypedParams<IncludedFieldsParams> getIncludedFields();

	ResourceInformation getResourceInformation();

	/**
	 * @return maximum number of resources to return or null for unbounded
	 */
	Long getLimit();

	/**
	 * @return maximum number of resources to skip in the response.
	 */
	public long getOffset();

	/**
	 * @return clone of this instance
	 */
	QueryAdapter duplicate();

	void setLimit(Long limit);

	void setOffset(long offset);

	/**
	 * The {@link QueryParams} instance for this query adapter if possible.
	 * @return may return null if the implementation does not support QueryParams
	 */
	QueryParams toQueryParams();

	/**
	 * The {@link QuerySpec} instance for this query adapter if possible.
	 * @return may return null if the implementation does not support QueryParams
	 */
	QuerySpec toQuerySpec();

}
