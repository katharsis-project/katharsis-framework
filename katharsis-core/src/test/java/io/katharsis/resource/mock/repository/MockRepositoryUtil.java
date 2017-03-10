package io.katharsis.resource.mock.repository;

import io.katharsis.core.internal.resource.AnnotationResourceInformationBuilder;
import io.katharsis.legacy.locator.JsonServiceLocator;
import io.katharsis.legacy.locator.SampleJsonServiceLocator;
import io.katharsis.legacy.registry.ResourceRegistryBuilder;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.resource.information.ResourceFieldNameTransformer;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.DefaultResourceLookup;
import io.katharsis.resource.registry.ResourceRegistry;

public class MockRepositoryUtil {

	public static void clear() {
		TaskRepository.clear();
		ProjectRepository.clear();
		TaskToProjectRepository.clear();
	}

	public static ResourceRegistry setupResourceRegistry() {
		JsonServiceLocator jsonServiceLocator = new SampleJsonServiceLocator();
		ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(new ResourceFieldNameTransformer());
		ModuleRegistry moduleRegistry = new ModuleRegistry();
		ResourceRegistryBuilder resourceRegistryBuilder = new ResourceRegistryBuilder(moduleRegistry, jsonServiceLocator, resourceInformationBuilder);
		DefaultResourceLookup resourceLookup = newResourceLookup();
		return resourceRegistryBuilder.build(resourceLookup, moduleRegistry, new ConstantServiceUrlProvider("http://127.0.0.1"));
	}

	public static DefaultResourceLookup newResourceLookup() {
		return new DefaultResourceLookup("io.katharsis.resource.mock");
	}

}
