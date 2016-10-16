package io.katharsis.jpa.internal.paging;

import io.katharsis.response.MetaInformation;

/**
 * TODO consider making this to a katharsis standard? Not specified JSON API.
 */
public interface PagedMetaInformation extends MetaInformation {

	public Long getTotalResourceCount();

	public void setTotalResourceCount(Long totalResourceCount);
}
