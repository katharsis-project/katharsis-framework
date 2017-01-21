package io.katharsis.resource.meta;

import io.katharsis.resource.list.ResourceList;

/**
 * Implement this class and provide the total number of (potentially filtered) resources
 * to let Katharsis compute pagination links. The meta information can be delivered
 * as part of the result by returning an instance of {@link ResourceList}
 * 
 * <p>
 * Note that in case of the use of LinksInformation,
 * PagedLinksInformation must be implemented as well. Otherwise a default links implementation is used.
 * </p>
 */
public interface PagedMetaInformation extends MetaInformation {

	public Long getTotalResourceCount();

	public void setTotalResourceCount(Long totalResourceCount);
}
