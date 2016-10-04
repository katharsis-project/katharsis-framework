package io.katharsis.jpa.internal.paging;

import io.katharsis.response.LinksInformation;

/**
 * Interface declration for any LinksInformation object holding 
 * paging information.
 *
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
