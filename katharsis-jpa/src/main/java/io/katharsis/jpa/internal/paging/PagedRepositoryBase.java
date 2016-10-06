package io.katharsis.jpa.internal.paging;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecMetaRepository;
import io.katharsis.response.paging.PagedResultList;

public abstract class PagedRepositoryBase<T> implements QuerySpecMetaRepository<T> {

	@Override
	public PagedMetaInformation getMetaInformation(Iterable<T> resources, QuerySpec querySpec) {
		PagedMetaInformation meta = newPagedMetaInformation();
		if (resources instanceof PagedResultList) {
			PagedResultList<T> pageResultList = (PagedResultList<T>) resources;
			meta.setTotalResourceCount(pageResultList.getTotalCount());
		}
		return meta;
	}

	protected abstract PagedMetaInformation newPagedMetaInformation();

}
