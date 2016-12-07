package io.katharsis.module;

import java.util.Arrays;
import java.util.List;

import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.field.ResourceField.ResourceFieldType;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.information.ResourceInformationBuilder;

public class TestResourceInformationBuilder implements ResourceInformationBuilder {

	@Override
	public boolean accept(Class<?> resourceClass) {
		return resourceClass == TestResource.class;
	}

	@Override
	public ResourceInformation build(Class<?> resourceClass) {
		ResourceField idField = new ResourceField("testId", "id", ResourceFieldType.ID, Integer.class, null);
		List<ResourceField> fields = Arrays.asList(idField);
		ResourceInformation info = new ResourceInformation(resourceClass, resourceClass.getSimpleName(), fields);
		return info;
	}

}