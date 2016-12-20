package io.katharsis.resource.include;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.internal.boot.EmptyPropertiesProvider;
import io.katharsis.internal.boot.KatharsisBootProperties;
import io.katharsis.internal.boot.PropertiesProvider;
import io.katharsis.jackson.JsonApiModuleBuilder;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.resource.Document;
import io.katharsis.resource.Relationship;
import io.katharsis.resource.Resource;
import io.katharsis.resource.ResourceId;
import io.katharsis.resource.internal.DocumentMapper;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.repository.MockRepositoryUtil;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.repository.adapter.RelationshipRepositoryAdapter;
import io.katharsis.resource.registry.repository.adapter.ResourceRepositoryAdapter;

@RunWith(MockitoJUnitRunner.class)
public class IncludeLookupSetterTest {

	protected ResourceRegistry resourceRegistry;

	private IncludeLookupSetter sut;

	private ObjectMapper objectMapper;

	private DocumentMapper documentMapper;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void setUp() throws Exception {
		MockRepositoryUtil.clear();

		// setup repositories
		resourceRegistry = MockRepositoryUtil.setupResourceRegistry();

		// setup mapping
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JsonApiModuleBuilder().build(resourceRegistry, false));
		documentMapper = new DocumentMapper(resourceRegistry, objectMapper);

		// get repositories
		ResourceRepositoryAdapter taskRepository = resourceRegistry.getEntry(Task.class).getResourceRepository(null);
		RelationshipRepositoryAdapter relRepositoryTaskToProject = resourceRegistry.getEntry(Task.class)
				.getRelationshipRepositoryForClass(Project.class, null);
		RelationshipRepositoryAdapter relRepositoryProjectToTask = resourceRegistry.getEntry(Project.class)
				.getRelationshipRepositoryForClass(Task.class, null);
		ResourceRepositoryAdapter projectRepository = resourceRegistry.getEntry(Project.class).getResourceRepository(null);

		// setup test data
		Project project = new Project();
		project.setId(2L);
		projectRepository.create(project, null);
		Task task = new Task();
		task.setId(1L);
		taskRepository.create(task, null);
		relRepositoryTaskToProject.setRelation(task, project.getId(), "includedProject", null);
		relRepositoryTaskToProject.setRelation(task, project.getId(), "project", null);
		relRepositoryTaskToProject.addRelations(task, Collections.singletonList(project.getId()), "includedProjects", null);

		// setup deep nested relationship
		Task includedTask = new Task();
		includedTask.setId(3L);
		taskRepository.create(includedTask, null);
		relRepositoryProjectToTask.setRelation(project, includedTask.getId(), "includedTask", null);
		Project deepIncludedProject = new Project();
		deepIncludedProject.setId(2L);
		projectRepository.create(project, null);
		relRepositoryTaskToProject.setRelation(includedTask, deepIncludedProject.getId(), "includedProject", null);
		relRepositoryTaskToProject.addRelations(includedTask, Collections.singletonList(project.getId()), "includedProjects",
				null);

		sut = new IncludeLookupSetter(resourceRegistry, documentMapper, new EmptyPropertiesProvider());
	}

	@After
	public void tearDown() {
		MockRepositoryUtil.clear();
	}

	protected QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());

	private void addParams(Map<String, Set<String>> params, String key, String value) {
		params.put(key, new HashSet<>(Arrays.asList(value)));
	}

	@Test
	public void includeOneRelationLookup() throws Exception {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "include[tasks]", "includedProject");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		QueryAdapter queryAdapter = new QueryParamsAdapter(queryParams);

		Resource task = new Resource();
		task.setId("1");
		Document doc = new Document();
		doc.setData(task);

		sut.setIncludedElements("tasks", doc, queryAdapter, null);
		Relationship relationship = task.getRelationships().get("includedProject");
		Assert.assertNotNull(relationship);
		Assert.assertNotNull(relationship.getSingleData());
	}

	@Test
	public void includeManyRelationLookup() throws Exception {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "include[tasks]", "includedProjects");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		QueryAdapter queryAdapter = new QueryParamsAdapter(queryParams);

		Resource task = new Resource();
		task.setId("1");
		Document doc = new Document();
		doc.setData(task);

		sut.setIncludedElements("tasks", doc, queryAdapter, null);
		Relationship relationship = task.getRelationships().get("includedProjects");
		Assert.assertNotNull(relationship);
		Assert.assertEquals(1, relationship.getCollectionData().size());
	}

	@Test
	public void includeOneDeepNestedRelationLookup() throws Exception {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "include[tasks]", "includedProject.includedTask.includedProject");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		QueryAdapter queryAdapter = new QueryParamsAdapter(queryParams);

		Resource task = new Resource();
		task.setId("3");
		Document doc = new Document();
		doc.setData(task);

		sut.setIncludedElements("tasks", doc, queryAdapter, null);

		Relationship relationship = task.getRelationships().get("includedProject");
		Assert.assertNotNull(relationship);
		List<ResourceId> relationshipData = relationship.getCollectionData();
		Assert.assertNotNull(relationshipData.get(0).getId());

		List<Resource> includes = doc.getIncluded();
		Assert.assertEquals(1, includes);
		Resource includedResource = includes.get(0);
		Assert.assertEquals("project", includedResource.getType());
		Assert.assertNotNull(includedResource.getRelationships().get("includedTask"));
		Assert.assertNotNull(includedResource.getRelationships().get("includedTask").getData());
	}

	@Test
	public void includeManyDeepNestedRelationLookup() throws Exception {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "include[tasks]", "includedProjects.includedTask.includedProject");

		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		QueryAdapter queryAdapter = new QueryParamsAdapter(queryParams);

		Resource task = new Resource();
		task.setId("3");
		Document doc = new Document();
		doc.setData(task);

		sut.setIncludedElements("tasks", doc, queryAdapter, null);
		Relationship relationship = task.getRelationships().get("includedProjects");
		Assert.assertNotNull(relationship);
		List<ResourceId> relationshipData = relationship.getCollectionData();
		Assert.assertNotNull(relationshipData.get(0).getId());

		List<Resource> includes = doc.getIncluded();
		Assert.assertEquals(2, includes);

		// FIXME more checks
	}

	@Test
	public void testNullPropertiesProviderResponse() throws Exception {
		sut = new IncludeLookupSetter(resourceRegistry, documentMapper, new PropertiesProvider() {

			@Override
			public String getProperty(String key) {
				return null;
			}
		});
	}

	@Test
	public void includePropertiesProviderAllTrueRelationshipLookup() throws Exception {
		sut = new IncludeLookupSetter(resourceRegistry, documentMapper, new PropertiesProvider() {

			@Override
			public String getProperty(String key) {
				return "true";
			}
		});

		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "include[tasks]", "project");

		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		QueryAdapter queryAdapter = new QueryParamsAdapter(queryParams);

		Resource task = new Resource();
		task.setId("1");
		Document doc = new Document();
		doc.setData(task);

		sut.setIncludedElements("tasks", doc, queryAdapter, null);
		Assert.assertNotNull(task.getRelationships().get("project"));
		Assert.assertNotNull(task.getRelationships().get("project").getData());
	}

	@Test
	public void includePropertiesProviderNonOverwriteRelationshipLookup() throws Exception {
		sut = new IncludeLookupSetter(resourceRegistry, documentMapper, new PropertiesProvider() {

			@Override
			public String getProperty(String key) {
				if (key.equalsIgnoreCase(KatharsisBootProperties.INCLUDE_AUTOMATICALLY_OVERWRITE)) {
					return "false";
				}
				return "true";
			}
		});

		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "include[tasks]", "project");

		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		QueryAdapter queryAdapter = new QueryParamsAdapter(queryParams);
		Project project = new Project();

		Relationship relationship = new Relationship();
		relationship.setData(new ResourceId("12", "project"));

		Resource task = new Resource();
		task.setId("1");
		task.getRelationships().put("project", relationship);
		Document doc = new Document();
		doc.setData(task);

		sut.setIncludedElements("tasks", doc, queryAdapter, null);
		Assert.assertNotNull(task.getRelationships().get("project"));
		Assert.assertTrue(task.getRelationships().get("project").getData() == project.getData());
	}
}
