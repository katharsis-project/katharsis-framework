package io.katharsis.resource.mock.repository;

import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.AnnotationResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.DefaultResourceLookup;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;

public class MockRepositoryUtil {

	public static void clear() {
		TaskRepository.clear();
		ProjectRepository.clear();
		TaskToProjectRepository.clear();		
	}

	public static ResourceRegistry setupResourceRegistry(){
		JsonServiceLocator jsonServiceLocator = new SampleJsonServiceLocator();
		ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(
				new ResourceFieldNameTransformer());
		ResourceRegistryBuilder resourceRegistryBuilder = new ResourceRegistryBuilder(jsonServiceLocator,
				resourceInformationBuilder);
		DefaultResourceLookup resourceLookup = newResourceLookup();
		return resourceRegistryBuilder.build(resourceLookup, new ConstantServiceUrlProvider("http://127.0.0.1"));
	}

	public static DefaultResourceLookup newResourceLookup() {
		return new DefaultResourceLookup("io.katharsis.resource.mock");
	}

}
