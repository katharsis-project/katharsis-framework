package io.katharsis.resource.list;

import java.util.List;

import io.katharsis.resource.links.LinksInformation;
import io.katharsis.resource.meta.MetaInformation;

/**
 * Holds links and meta information next to the actual list. Can be returned by findAll and findTargets repository operation.	
 */
public interface ResourceList<T> extends List<T> {

	public LinksInformation getLinks();

	public MetaInformation getMeta();

	/**
	 * @param linksClass to return
	 * @return links of the given type or null if not available
	 */
	public <L extends LinksInformation> L getLinks(Class<L> linksClass);

	/**
	 * @param metaClass to return
	 * @return meta information of the given type or null if not available
	 */
	public <M extends MetaInformation> M getMeta(Class<M> metaClass);
}
