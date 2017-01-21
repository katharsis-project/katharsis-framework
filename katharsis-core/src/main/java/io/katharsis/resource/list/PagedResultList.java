package io.katharsis.resource.list;

import java.util.List;

import io.katharsis.core.internal.utils.WrappedList;
import io.katharsis.resource.meta.PagedMetaInformation;

/**
 * Use this class as return type and provide the total number of (potentially filtered) 
 * to let Katharsis compute pagination links. Note that in case of the use of LinksInformation,
 * PagedLinksInformation must be implemented. Otherwise a default implementation is used.
 * 
 * @Deprecated It is recommended to to implement {@link PagedMetaInformation} instead and use in combination with {@link ResourceList}.
 */
@Deprecated
public class PagedResultList<T> extends WrappedList<T> {

	private Long totalCount;

	public PagedResultList(List<T> list, Long totalCount) {
		super(list);
		this.totalCount = totalCount;
	}

	public Long getTotalCount() {
		return totalCount;
	}
}
