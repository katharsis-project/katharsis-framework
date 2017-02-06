package io.katharsis.resource.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.katharsis.core.internal.boot.EmptyPropertiesProvider;
import io.katharsis.core.internal.boot.PropertiesProvider;
import io.katharsis.core.internal.repository.adapter.RelationshipRepositoryAdapter;
import io.katharsis.core.internal.repository.adapter.ResourceRepositoryAdapter;
import io.katharsis.core.internal.resource.DocumentMapper;
import io.katharsis.core.properties.KatharsisProperties;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.resource.Document;
import io.katharsis.resource.Relationship;
import io.katharsis.resource.Resource;
import io.katharsis.resource.ResourceIdentifier;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.User;
import io.katharsis.resource.registry.RegistryEntry;

@RunWith(MockitoJUnitRunner.class)
public class IncludeLookupSetterTest extends AbstractDocumentMapperTest {

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Before
	public void setup() {
		super.setup();

		// get repositories
		ResourceRepositoryAdapter taskRepository = resourceRegistry.findEntry(Task.class).getResourceRepository(null);
		RelationshipRepositoryAdapter relRepositoryTaskToProject = resourceRegistry.findEntry(Task.class).getRelationshipRepositoryForClass(Project.class, null);
		RelationshipRepositoryAdapter relRepositoryProjectToTask = resourceRegistry.findEntry(Project.class).getRelationshipRepositoryForClass(Task.class, null);
		ResourceRepositoryAdapter projectRepository = resourceRegistry.findEntry(Project.class).getResourceRepository(null);

		// setup test data
		
		ResourceInformation taskInfo = resourceRegistry.findEntry(Task.class).getResourceInformation();
		ResourceInformation projectInfo = resourceRegistry.findEntry(Project.class).getResourceInformation();
		ResourceField includedTaskField = projectInfo.findRelationshipFieldByName("includedTask");
		ResourceField includedProjectField = taskInfo.findRelationshipFieldByName("includedProject");
		ResourceField includedProjectsField = taskInfo.findRelationshipFieldByName("includedProjects");
		ResourceField projectField = taskInfo.findRelationshipFieldByName("project");
		
		
		Project project = new Project();
		project.setId(2L);
		projectRepository.create(project, null);
		Task task = new Task();
		task.setId(1L);
		taskRepository.create(task, null);
		relRepositoryTaskToProject.setRelation(task, project.getId(), includedProjectField, null);
		relRepositoryTaskToProject.setRelation(task, project.getId(), projectField, null);
		relRepositoryTaskToProject.addRelations(task, Collections.singletonList(project.getId()), includedProjectsField, null);

		// setup deep nested relationship
		Task includedTask = new Task();
		includedTask.setId(3L);
		taskRepository.create(includedTask, null);
		relRepositoryProjectToTask.setRelation(project, includedTask.getId(), includedTaskField, null);
		Project deepIncludedProject = new Project();
		deepIncludedProject.setId(2L);
		projectRepository.create(project, null);
		relRepositoryTaskToProject.setRelation(includedTask, deepIncludedProject.getId(), includedProjectField, null);
		relRepositoryTaskToProject.addRelations(includedTask, Collections.singletonList(project.getId()), includedProjectsField, null);
	}

	@Test
	public void includeOneRelationLookup() throws Exception {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.includeRelation(Arrays.asList("includedProject"));

		Task task = new Task();
		task.setId(1L);

		Document document = mapper.toDocument(toResponse(task), toAdapter(querySpec));
		Resource taskResource = document.getSingleData().get();

		Relationship relationship = taskResource.getRelationships().get("includedProject");
		assertNotNull(relationship);
		assertNotNull(relationship.getSingleData());

		List<Resource> included = document.getIncluded();
		assertEquals(1, included.size());
	}

	@Test
	public void includeManyRelationLookup() throws Exception {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.includeRelation(Arrays.asList("includedProjects"));

		Task task = new Task();
		task.setId(1L);

		Document document = mapper.toDocument(toResponse(task), toAdapter(querySpec));
		Resource taskResource = document.getSingleData().get();

		Relationship relationship = taskResource.getRelationships().get("includedProjects");
		assertNotNull(relationship);
		assertEquals(1, relationship.getCollectionData().get().size());
	}

	@Test
	public void includeOneDeepNestedRelationLookup() throws Exception {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.includeRelation(Arrays.asList("includedProject", "includedTask", "includedProject"));

		Task task = new Task();
		task.setId(3L);

		Document document = mapper.toDocument(toResponse(task), toAdapter(querySpec));
		Resource taskResource = document.getSingleData().get();

		Relationship oneRelationship = taskResource.getRelationships().get("includedProject");
		assertNotNull(oneRelationship);
		assertNotNull(oneRelationship.getSingleData());

		List<Resource> includes = document.getIncluded();
		assertEquals(1, includes.size());
		Resource includedResource = includes.get(0);
		assertEquals("projects", includedResource.getType());
		assertNotNull(includedResource.getRelationships().get("includedTask"));
		assertNotNull(includedResource.getRelationships().get("includedTask").getData());
	}

	@Test
	public void includeManyDeepNestedRelationLookup() throws Exception {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.includeRelation(Arrays.asList("includedProjects", "includedTask", "includedProject"));

		Task task = new Task();
		task.setId(3L);

		Document document = mapper.toDocument(toResponse(task), toAdapter(querySpec));
		Resource taskResource = document.getSingleData().get();

		Relationship manyRelationship = taskResource.getRelationships().get("includedProjects");
		assertNotNull(manyRelationship);
		List<ResourceIdentifier> relationshipData = manyRelationship.getCollectionData().get();
		assertNotNull(relationshipData.get(0).getId());

		List<Resource> includes = document.getIncluded();
		assertEquals(1, includes.size());
		Resource includedResource = includes.get(0);
		assertEquals("projects", includedResource.getType());
		assertNotNull(includedResource.getRelationships().get("includedTask"));
		assertNotNull(includedResource.getRelationships().get("includedTask").getData());
	}

	@Test
	public void includePropertiesProviderAllTrueRelationshipLookup() throws Exception {
		PropertiesProvider propertiesProvider = new PropertiesProvider() {

			@Override
			public String getProperty(String key) {
				if (key.equalsIgnoreCase(KatharsisProperties.INCLUDE_AUTOMATICALLY_OVERWRITE)) {
					return "true";
				}
				return "true";
			}
		};
		mapper = new DocumentMapper(resourceRegistry, objectMapper, propertiesProvider);

		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.includeRelation(Arrays.asList("project"));

		Task task = new Task();
		task.setId(1L);

		Document document = mapper.toDocument(toResponse(task), toAdapter(querySpec));
		Resource taskResource = document.getSingleData().get();

		assertNotNull(taskResource.getRelationships().get("project"));
		assertNotNull(taskResource.getRelationships().get("project").getData());
	}

	@Test
	public void includePropertiesProviderNonOverwriteRelationshipLookup() throws Exception {
		PropertiesProvider propertiesProvider = new PropertiesProvider() {

			@Override
			public String getProperty(String key) {
				if (key.equalsIgnoreCase(KatharsisProperties.INCLUDE_AUTOMATICALLY_OVERWRITE)) {
					return "false";
				}
				return "true";
			}
		};
		mapper = new DocumentMapper(resourceRegistry, objectMapper, propertiesProvider);

		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.includeRelation(Arrays.asList("project"));

		Project project = new Project();
		project.setId(12L);

		Task task = new Task();
		task.setId(1L);
		task.setProject(project);

		Document document = mapper.toDocument(toResponse(task), toAdapter(querySpec));
		Resource taskResource = document.getSingleData().get();

		Relationship relationship = taskResource.getRelationships().get("project");
		assertNotNull(relationship);
		assertEquals(relationship.getSingleData().get().getId(), "12");
	}

	@Test
	public void includeByDefaultSerializeNLevels() throws Exception {
		Project project = new Project();
		project.setId(1L);

		Task task = new Task().setId(2L);
		project.setTask(task);

		Project projectDefault = new Project().setId(3L);
		task.setProject(projectDefault);


		mapper = new DocumentMapper(resourceRegistry, objectMapper, new EmptyPropertiesProvider());

		QuerySpec querySpec = new QuerySpec(Project.class);
		querySpec.includeRelation(Collections.singletonList("task"));

		Document document = mapper.toDocument(toResponse(project), toAdapter(querySpec));
		Resource projectResource = document.getSingleData().get();

		Relationship relationship = projectResource.getRelationships().get("task");
		assertNotNull(relationship);
		assertEquals("2", relationship.getSingleData().get().getId());

		assertNotNull(document.getIncluded());
		assertEquals(2, document.getIncluded().size());
		List<Resource> resources = document.getIncluded();
		assertEquals("projects", resources.get(0).getType());
		assertEquals("3", resources.get(0).getId());
		assertEquals("tasks", resources.get(1).getType());
		assertEquals("2", resources.get(1).getId());

	}
}
