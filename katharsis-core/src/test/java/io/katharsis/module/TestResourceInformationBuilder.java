package io.katharsis.module;

import java.util.Arrays;
import java.util.List;

import io.katharsis.core.internal.resource.ResourceFieldImpl;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.information.ResourceFieldType;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInformationBuilderContext;
import io.katharsis.utils.parser.TypeParser;

public class TestResourceInformationBuilder implements ResourceInformationBuilder {

	private ResourceInformationBuilderContext context;

	@Override
	public boolean accept(Class<?> resourceClass) {
		return resourceClass == TestResource.class;
	}

	@Override
	public ResourceInformation build(Class<?> resourceClass) {
		ResourceField idField = new ResourceFieldImpl("testId", "id", ResourceFieldType.ID, Integer.class, null, null);
		List<ResourceField> fields = Arrays.asList(idField);
		TypeParser typeParser = context.getTypeParser();
		ResourceInformation info = new ResourceInformation(typeParser, resourceClass, resourceClass.getSimpleName(), fields);
		return info;
	}

	@Override
	public String getResourceType(Class<?> clazz) {
		return "testId";
	}

	@Override
	public void init(ResourceInformationBuilderContext context) {
		this.context = context;
	}

}