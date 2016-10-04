package io.katharsis.jpa.internal.paging;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecLinksRepository;
import io.katharsis.queryspec.QuerySpecMetaRepository;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryAware;
import io.katharsis.utils.JsonApiUrlBuilder;

public abstract class PagedRepositoryBase<T>
		implements ResourceRegistryAware, QuerySpecMetaRepository<T>, QuerySpecLinksRepository<T> {

	private ResourceRegistry resourceRegistry;

	@Override
	public void setResourceRegistry(ResourceRegistry resourceRegistry) {
		this.resourceRegistry = resourceRegistry;
	}

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

	@Override
	public PagedLinksInformation getLinksInformation(Iterable<T> resources, QuerySpec querySpec) {
		PagedLinksInformation links = newPagedLinksInformation();
		if (resources instanceof PagedResultList && querySpec.getLimit() != null) {
			PagedResultList<T> pageResultList = (PagedResultList<T>) resources;

			long total = pageResultList.getTotalCount();
			long pageSize = querySpec.getLimit().longValue();
			long offset = querySpec.getOffset();

			long currentPage = offset / pageSize;
			if (currentPage * pageSize != offset) {
				throw new IllegalArgumentException("offset " + offset + " is not a multiple of limit " + pageSize);
			}
			long totalPages = (total + pageSize - 1) / pageSize;

			QuerySpec pageSpec = querySpec.duplicate();
			pageSpec.setLimit(pageSize);

			pageSpec.setOffset(0);
			links.setFirst(toUrl(pageSpec, pageResultList));

			pageSpec.setOffset((totalPages - 1) * pageSize);
			links.setLast(toUrl(pageSpec, pageResultList));

			if (currentPage > 0) {
				pageSpec.setOffset((currentPage - 1) * pageSize);
				links.setPrev(toUrl(pageSpec, pageResultList));
			}

			if (currentPage < totalPages - 1) {
				pageSpec.setOffset((currentPage + 1) * pageSize);
				links.setNext(toUrl(pageSpec, pageResultList));
			}
		}
		return links;
	}

	protected abstract PagedLinksInformation newPagedLinksInformation();

	private String toUrl(QuerySpec querySpec, PagedResultList<T> pagedResultList) {
		JsonApiUrlBuilder urlBuilder = new JsonApiUrlBuilder(resourceRegistry);
		Object relationshipSourceId = pagedResultList.getRelationshipSourceId();
		String relationshipField = pagedResultList.getRelationshipField();
		Class<?> rootClass;
		if (relationshipField == null) {
			rootClass = querySpec.getResourceClass();
		}
		else {
			rootClass = pagedResultList.getRelationshipSourceClass();
		}
		return urlBuilder.buildUrl(rootClass, relationshipSourceId, querySpec, relationshipField);
	}
}
