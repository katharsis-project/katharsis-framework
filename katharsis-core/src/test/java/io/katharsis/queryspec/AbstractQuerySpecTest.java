package io.katharsis.queryspec;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;

import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.AnnotationResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.registry.DefaultResourceLookup;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;

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
		DefaultResourceLookup resourceLookup = new DefaultResourceLookup(Task.class.getPackage().getName() + "," + getClass().getPackage().getName());
		resourceRegistry = resourceRegistryBuilder.build(resourceLookup, "http://127.0.0.1");
		FilterOperatorRegistry operators = new FilterOperatorRegistry();
		operators.register(FilterOperator.EQ);
		operators.register(FilterOperator.NEQ);
		operators.register(FilterOperator.GE);
		operators.register(FilterOperator.GT);
		operators.register(FilterOperator.LE);
		operators.register(FilterOperator.LT);
		operators.setDefaultOperator(FilterOperator.EQ);
		parser = new DefaultQuerySpecConverter(resourceRegistry, operators);
	}

	protected static void addParams(Map<String, Set<String>> params, String key, String value) {
		params.put(key, new HashSet<String>(Arrays.asList(value)));
	}
}
