package io.katharsis.resource.information;

public interface ResourceInformationBuilderContext {

	public String getResourceType(Class<?> clazz);

	public boolean accept(Class<?> type);
}
