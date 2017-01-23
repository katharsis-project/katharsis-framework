package io.katharsis.queryspec;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;

import io.katharsis.core.internal.resource.AnnotationResourceInformationBuilder;
import io.katharsis.legacy.internal.DefaultQuerySpecConverter;
import io.katharsis.legacy.locator.JsonServiceLocator;
import io.katharsis.legacy.locator.SampleJsonServiceLocator;
import io.katharsis.legacy.queryParams.DefaultQueryParamsParser;
import io.katharsis.legacy.queryParams.QueryParamsBuilder;
import io.katharsis.legacy.registry.ResourceRegistryBuilder;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.resource.information.ResourceFieldNameTransformer;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.repository.ScheduleRepositoryImpl;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.DefaultResourceLookup;
import io.katharsis.resource.registry.ResourceRegistry;

public abstract class AbstractQuerySpecTest {

	protected DefaultQuerySpecConverter parser;

	protected QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());

	protected ResourceRegistry resourceRegistry;

	@Before
	public void setup() {
		JsonServiceLocator jsonServiceLocator = new SampleJsonServiceLocator();
		ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(
				new ResourceFieldNameTransformer());
		ResourceRegistryBuilder resourceRegistryBuilder = new ResourceRegistryBuilder(jsonServiceLocator,
				resourceInformationBuilder);
		DefaultResourceLookup resourceLookup = newResourceLookup();
		resourceRegistry = resourceRegistryBuilder.build(resourceLookup, new ModuleRegistry(),
				new ConstantServiceUrlProvider("http://127.0.0.1"));
		parser = new DefaultQuerySpecConverter(resourceRegistry);
	}

	protected DefaultResourceLookup newResourceLookup() {
		return new DefaultResourceLookup(Task.class.getPackage().getName() + "," + getClass().getPackage().getName()) {

			@Override
			public Set<Class<?>> getResourceRepositoryClasses() {
				Set<Class<?>> set = new HashSet<>();
				set.addAll(super.getResourceRepositoryClasses());
				set.add(ScheduleRepositoryImpl.class); // not yet recognized by reflections for some reason
				return set;
			}
		};
	}

	protected static void addParams(Map<String, Set<String>> params, String key, String value) {
		params.put(key, new HashSet<String>(Arrays.asList(value)));
	}
}
