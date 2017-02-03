package io.katharsis.legacy.registry;

import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInformationBuilderContext;

public class DefaultResourceInformationBuilderContext implements ResourceInformationBuilderContext {

	private ResourceInformationBuilder builder;

	public DefaultResourceInformationBuilderContext(ResourceInformationBuilder builder) {
		this.builder = builder;
	}

	@Override
	public String getResourceType(Class<?> clazz) {
		return builder.getResourceType(clazz);
	}

	@Override
	public boolean accept(Class<?> type) {
		return builder.accept(type);
	}
}
