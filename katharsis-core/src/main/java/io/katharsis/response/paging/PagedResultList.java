package io.katharsis.response.paging;

import java.util.List;

import io.katharsis.utils.WrappedList;

/**
 * Use this class as return type and provide the total number of (potentially filtered) 
 * to let Katharsis compute pagination links. Note that in case of the use of LinksInformation,
 * PagedLinksInformation must be implemented. Otherwise a default implementation is used.
 */
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
