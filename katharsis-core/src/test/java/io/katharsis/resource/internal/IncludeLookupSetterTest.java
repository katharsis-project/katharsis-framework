package io.katharsis.resource.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.katharsis.internal.boot.KatharsisBootProperties;
import io.katharsis.internal.boot.PropertiesProvider;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.resource.Document;
import io.katharsis.resource.Relationship;
import io.katharsis.resource.Resource;
import io.katharsis.resource.ResourceIdentifier;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.registry.repository.adapter.RelationshipRepositoryAdapter;
import io.katharsis.resource.registry.repository.adapter.ResourceRepositoryAdapter;

@RunWith(MockitoJUnitRunner.class)
public class IncludeLookupSetterTest extends AbstractDocumentMapperTest {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void setup() {
		super.setup();

		// get repositories
		ResourceRepositoryAdapter taskRepository = resourceRegistry.getEntry(Task.class).getResourceRepository(null);
		RelationshipRepositoryAdapter relRepositoryTaskToProject = resourceRegistry.getEntry(Task.class).getRelationshipRepositoryForClass(Project.class, null);
		RelationshipRepositoryAdapter relRepositoryProjectToTask = resourceRegistry.getEntry(Project.class).getRelationshipRepositoryForClass(Task.class, null);
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
		relRepositoryTaskToProject.addRelations(includedTask, Collections.singletonList(project.getId()), "includedProjects", null);
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
		Assert.assertNotNull(relationship);
		Assert.assertNotNull(relationship.getSingleData());

		List<Resource> included = document.getIncluded();
		Assert.assertEquals(1, included.size());
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
		Assert.assertNotNull(relationship);
		Assert.assertEquals(1, relationship.getCollectionData().get().size());
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
		Assert.assertNotNull(oneRelationship);
		Assert.assertNotNull(oneRelationship.getSingleData());

		List<Resource> includes = document.getIncluded();
		Assert.assertEquals(1, includes.size());
		Resource includedResource = includes.get(0);
		Assert.assertEquals("projects", includedResource.getType());
		Assert.assertNotNull(includedResource.getRelationships().get("includedTask"));
		Assert.assertNotNull(includedResource.getRelationships().get("includedTask").getData());
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
		Assert.assertNotNull(manyRelationship);
		List<ResourceIdentifier> relationshipData = manyRelationship.getCollectionData().get();
		Assert.assertNotNull(relationshipData.get(0).getId());

		List<Resource> includes = document.getIncluded();
		Assert.assertEquals(1, includes.size());
		Resource includedResource = includes.get(0);
		Assert.assertEquals("projects", includedResource.getType());
		Assert.assertNotNull(includedResource.getRelationships().get("includedTask"));
		Assert.assertNotNull(includedResource.getRelationships().get("includedTask").getData());
	}

	@Test
	public void includePropertiesProviderAllTrueRelationshipLookup() throws Exception {
		PropertiesProvider propertiesProvider = new PropertiesProvider() {

			@Override
			public String getProperty(String key) {
				if (key.equalsIgnoreCase(KatharsisBootProperties.INCLUDE_AUTOMATICALLY_OVERWRITE)) {
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

		Assert.assertNotNull(taskResource.getRelationships().get("project"));
		Assert.assertNotNull(taskResource.getRelationships().get("project").getData());
	}

	@Test
	public void includePropertiesProviderNonOverwriteRelationshipLookup() throws Exception {
		PropertiesProvider propertiesProvider = new PropertiesProvider() {

			@Override
			public String getProperty(String key) {
				if (key.equalsIgnoreCase(KatharsisBootProperties.INCLUDE_AUTOMATICALLY_OVERWRITE)) {
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
		Assert.assertNotNull(relationship);
		Assert.assertEquals(relationship.getSingleData().get().getId(), "12");
	}
}
