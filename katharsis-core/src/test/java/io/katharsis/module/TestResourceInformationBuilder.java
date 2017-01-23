package io.katharsis.module;

import java.util.Arrays;
import java.util.List;

import io.katharsis.core.internal.resource.ResourceFieldImpl;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.information.ResourceFieldType;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInformationBuilderContext;

public class TestResourceInformationBuilder implements ResourceInformationBuilder {

	@Override
	public boolean accept(Class<?> resourceClass) {
		return resourceClass == TestResource.class;
	}

	@Override
	public ResourceInformation build(Class<?> resourceClass) {
		ResourceField idField = new ResourceFieldImpl("testId", "id", ResourceFieldType.ID, Integer.class, null, null);
		List<ResourceField> fields = Arrays.asList(idField);
		ResourceInformation info = new ResourceInformation(resourceClass, resourceClass.getSimpleName(), fields);
		return info;
	}

	@Override
	public String getResourceType(Class<?> clazz) {
		return "testId";
	}

	@Override
	public void init(ResourceInformationBuilderContext context) {
	}

}