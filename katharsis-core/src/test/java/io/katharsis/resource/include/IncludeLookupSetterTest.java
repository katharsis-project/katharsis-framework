package io.katharsis.resource.include;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.AnnotationResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.DefaultResourceLookup;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.resource.registry.responseRepository.RelationshipRepositoryAdapter;
import io.katharsis.resource.registry.responseRepository.ResourceRepositoryAdapter;

@RunWith(MockitoJUnitRunner.class)
public class IncludeLookupSetterTest {

	protected ResourceRegistry resourceRegistry;

	private IncludeLookupSetter sut;

	protected DefaultResourceLookup newResourceLookup() {
		return new DefaultResourceLookup("io.katharsis.resource.mock");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void setUp() throws Exception {
		// setup repositories
		JsonServiceLocator jsonServiceLocator = new SampleJsonServiceLocator();
		ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(
				new ResourceFieldNameTransformer());
		ResourceRegistryBuilder resourceRegistryBuilder = new ResourceRegistryBuilder(jsonServiceLocator,
				resourceInformationBuilder);
		DefaultResourceLookup resourceLookup = newResourceLookup();
		resourceRegistry = resourceRegistryBuilder.build(resourceLookup, new ConstantServiceUrlProvider("http://127.0.0.1"));

		// get repositories
		ResourceRepositoryAdapter taskRepository = resourceRegistry.getEntry(Task.class).getResourceRepository(null);
		RelationshipRepositoryAdapter relRepository = resourceRegistry.getEntry(Task.class)
				.getRelationshipRepositoryForClass(Project.class, null);
		ResourceRepositoryAdapter projectRepository = resourceRegistry.getEntry(Project.class).getResourceRepository(null);

		// setup test data
		Project project = new Project();
		project.setId(2L);
		projectRepository.save(project, null);
		Task task = new Task();
		task.setId(1L);
		taskRepository.save(task, null);
		relRepository.setRelation(task, project.getId(), "includedProject", null);
		relRepository.addRelations(task, Arrays.asList(project.getId()), "includedProjects", null);

		sut = new IncludeLookupSetter(resourceRegistry);
	}

	protected QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());

	private void addParams(Map<String, Set<String>> params, String key, String value) {
		params.put(key, new HashSet<String>(Arrays.asList(value)));
	}

	@Test
	public void includeOneRelationLookup() throws Exception {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "include[tasks]", "includedProject");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		QueryAdapter queryAdapter = new QueryParamsAdapter(queryParams);

		Task task = new Task();
		task.setId(1L);
		sut.setIncludedElements("tasks", task, queryAdapter, null);
		Assert.assertNotNull(task.getIncludedProject());
	}

	@Test
	public void includeManyRelationLookup() throws Exception {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "include[tasks]", "includedProjects");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		QueryAdapter queryAdapter = new QueryParamsAdapter(queryParams);

		Task task = new Task();
		task.setId(1L);

		sut.setIncludedElements("tasks", task, queryAdapter, null);
		Assert.assertNotNull(task.getIncludedProjects());
		Assert.assertEquals(1, task.getIncludedProjects().size());
	}
}
