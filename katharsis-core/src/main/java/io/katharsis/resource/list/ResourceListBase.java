package io.katharsis.resource.list;

import io.katharsis.resource.links.LinksInformation;
import io.katharsis.resource.meta.MetaInformation;

/**
 * Helper implementation that can be used as base class for custom {@link ResourceList} implementation. It carries
 * further type parameters to specify the type of meta and links information without having to override their
 * respective methods.
 *
 * @param <T> resource type
 * @param <M> meta type
 * @param <L> links type
 */
public abstract class ResourceListBase<T, M extends MetaInformation, L extends LinksInformation> extends DefaultResourceList<T> {

	@SuppressWarnings("unchecked")
	@Override
	public L getLinks() {
		return (L) links;
	}

	@SuppressWarnings("unchecked")
	@Override
	public M getMeta() {
		return (M) meta;
	}
}