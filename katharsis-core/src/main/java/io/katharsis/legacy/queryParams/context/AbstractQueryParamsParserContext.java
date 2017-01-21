package io.katharsis.legacy.queryParams.context;

import io.katharsis.core.internal.dispatcher.path.JsonPath;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.ResourceRegistry;

/**
 * @deprecated make use of QuerySpec
 */
@Deprecated
public abstract class AbstractQueryParamsParserContext implements QueryParamsParserContext {

	private final ResourceInformation resourceInformation;

	protected AbstractQueryParamsParserContext(ResourceRegistry resourceRegistry, JsonPath path) {
		resourceInformation = resourceRegistry.getEntry(path.getResourceName()).getResourceInformation();
	}

	@Override
	public ResourceInformation getRequestedResourceInformation() {
		return resourceInformation;
	}
}
