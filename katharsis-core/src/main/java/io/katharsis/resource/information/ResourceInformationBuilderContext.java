package io.katharsis.resource.information;

import io.katharsis.utils.parser.TypeParser;

public interface ResourceInformationBuilderContext {

	public String getResourceType(Class<?> clazz);

	public boolean accept(Class<?> type);

	public TypeParser getTypeParser();
}
