package io.katharsis.jpa.internal.paging;

import io.katharsis.response.MetaInformation;

public interface PagedMetaInformation extends MetaInformation {

	public Long getTotalResourceCount();

	public void setTotalResourceCount(Long totalResourceCount);
}
