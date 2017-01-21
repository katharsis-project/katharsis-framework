package io.katharsis.queryspec.internal;

import io.katharsis.legacy.queryParams.params.IncludedFieldsParams;
import io.katharsis.legacy.queryParams.params.IncludedRelationsParams;
import io.katharsis.legacy.queryParams.params.TypedParams;

public interface QueryAdapter {

	boolean hasIncludedRelations();

	TypedParams<IncludedRelationsParams> getIncludedRelations();

	TypedParams<IncludedFieldsParams> getIncludedFields();

	Class<?> getResourceClass();

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

}
