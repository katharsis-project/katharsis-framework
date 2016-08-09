package io.katharsis.module;

import java.util.HashSet;
import java.util.Set;

import io.katharsis.resource.field.ResourceAttributesBridge;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.information.ResourceInformationBuilder;

class TestResourceInformationBuilder implements ResourceInformationBuilder {

	@Override
	public boolean accept(Class<?> resourceClass) {
		return resourceClass == TestResource.class;
	}

	@Override
	public ResourceInformation build(Class<?> resourceClass) {
		ResourceField idField = new ResourceField("testId", "id", Integer.class, null);
		ResourceAttributesBridge<?> attributeFields = null;
		Set<ResourceField> relationshipFields = new HashSet<ResourceField>();
		ResourceInformation info = new ResourceInformation(resourceClass, resourceClass.getSimpleName(), idField,
				attributeFields, relationshipFields);
		return info;
	}

}