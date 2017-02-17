package io.katharsis.legacy.registry;

import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInformationBuilderContext;
import io.katharsis.utils.parser.TypeParser;

public class DefaultResourceInformationBuilderContext implements ResourceInformationBuilderContext {

	private ResourceInformationBuilder builder;
	private TypeParser typeParser;

	public DefaultResourceInformationBuilderContext(ResourceInformationBuilder builder, TypeParser typeParser) {
		this.builder = builder;
		this.typeParser = typeParser;
	}

	@Override
	public String getResourceType(Class<?> clazz) {
		return builder.getResourceType(clazz);
	}

	@Override
	public boolean accept(Class<?> type) {
		return builder.accept(type);
	}

	@Override
	public TypeParser getTypeParser() {
		return typeParser;
	}
}
