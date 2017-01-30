package io.katharsis.resource.links;

import io.katharsis.resource.list.PagedResultList;

/**
 * Interface declaration for any LinksInformation object holding 
 * paging information. This interface must be implemented if a
 * repository returns a {@link PagedResultList}. {@link DefaultPagedLinksInformation}
 * provides a default implementation.
 */
public interface PagedLinksInformation extends LinksInformation {

	public String getFirst();

	public void setFirst(String first);

	public String getLast();

	public void setLast(String last);

	public String getNext();

	public void setNext(String next);

	public String getPrev();

	public void setPrev(String prev);
}
