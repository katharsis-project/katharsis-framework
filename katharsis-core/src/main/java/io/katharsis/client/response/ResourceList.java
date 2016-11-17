package io.katharsis.client.response;

import java.util.List;

import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;

/**
 * @deprecated Make use of ResourceList, ResourceListBase and DefaultResourceList of katharsis-core instead
 */
@Deprecated
public interface ResourceList<T> extends List<T> {

	/**
	 * @deprecated Make use of ResourceList, ResourceListBase and DefaultResourceList of katharsis-core instead
	 */
	@Deprecated
	public LinksInformation getLinksInformation();

	/**
	 * @deprecated Make use of ResourceList, ResourceListBase and DefaultResourceList of katharsis-core instead
	 */
	@Deprecated
	public MetaInformation getMetaInformation();

	/**
	 * @deprecated Make use of ResourceList, ResourceListBase and DefaultResourceList of katharsis-core instead
	 */
	@Deprecated
	public <L extends LinksInformation> L getLinksInformation(Class<L> linksClass);

	/**
	 * @deprecated Make use of ResourceList, ResourceListBase and DefaultResourceList of katharsis-core instead
	 */
	@Deprecated
	public <M extends MetaInformation> M getMetaInformation(Class<M> metaClass);
}
