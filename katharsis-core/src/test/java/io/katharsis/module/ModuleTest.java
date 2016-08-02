package io.katharsis.module;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.dispatcher.filter.Filter;
import io.katharsis.dispatcher.filter.TestFilter;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.mock.models.ComplexPojo;
import io.katharsis.resource.mock.models.Document;
import io.katharsis.resource.mock.models.FancyProject;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.Thing;
import io.katharsis.resource.mock.models.User;
import io.katharsis.resource.mock.repository.DocumentRepository;
import io.katharsis.resource.mock.repository.PojoRepository;
import io.katharsis.resource.mock.repository.ProjectRepository;
import io.katharsis.resource.mock.repository.ResourceWithoutRepositoryToProjectRepository;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import io.katharsis.resource.mock.repository.TaskWithLookupRepository;
import io.katharsis.resource.mock.repository.UserRepository;
import io.katharsis.resource.mock.repository.UserToProjectRepository;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceLookup;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.repository.DirectResponseRelationshipEntry;

public class ModuleTest {

	private ResourceRegistry resourceRegistry;
	private ModuleRegistry moduleRegistry;

	@Before
	public void setup() {
		resourceRegistry = new ResourceRegistry("http://localhost");

		moduleRegistry = new ModuleRegistry();
		moduleRegistry.addModule(new CoreModule("io.katharsis.module.mock", new ResourceFieldNameTransformer()));
		moduleRegistry.addModule(new TestModule());
		moduleRegistry.init(new ObjectMapper(), resourceRegistry);
	}

	@Test
	public void testInformationBuilder() throws Exception {
		ResourceInformationBuilder informationBuilder = moduleRegistry.getResourceInformationBuilder();

		Assert.assertTrue(informationBuilder.accept(ComplexPojo.class));
		Assert.assertTrue(informationBuilder.accept(Document.class));
		Assert.assertTrue(informationBuilder.accept(FancyProject.class));
		Assert.assertTrue(informationBuilder.accept(Project.class));
		Assert.assertTrue(informationBuilder.accept(Task.class));
		Assert.assertTrue(informationBuilder.accept(Thing.class));
		Assert.assertTrue(informationBuilder.accept(User.class));
		Assert.assertTrue(informationBuilder.accept(TestResource.class));

		Assert.assertFalse(informationBuilder.accept(TestRepository.class));
		Assert.assertFalse(informationBuilder.accept(DocumentRepository.class));
		Assert.assertFalse(informationBuilder.accept(PojoRepository.class));
		Assert.assertFalse(informationBuilder.accept(ProjectRepository.class));
		Assert.assertFalse(informationBuilder.accept(ResourceWithoutRepositoryToProjectRepository.class));
		Assert.assertFalse(informationBuilder.accept(TaskToProjectRepository.class));
		Assert.assertFalse(informationBuilder.accept(TaskWithLookupRepository.class));
		Assert.assertFalse(informationBuilder.accept(UserRepository.class));
		Assert.assertFalse(informationBuilder.accept(UserToProjectRepository.class));

		Assert.assertFalse(informationBuilder.accept(Object.class));
		Assert.assertFalse(informationBuilder.accept(String.class));

		try {
			informationBuilder.build(Object.class);
			Assert.fail();
		} catch (UnsupportedOperationException e) {
			// ok
		}

		ResourceInformation userInfo = informationBuilder.build(User.class);
		Assert.assertEquals("id", userInfo.getIdField().getUnderlyingName());

		ResourceInformation testInfo = informationBuilder.build(TestResource.class);
		Assert.assertEquals("id", testInfo.getIdField().getUnderlyingName());
		Assert.assertEquals("testId", testInfo.getIdField().getJsonName());
	}

	@Test
	public void testResourceLookup() throws Exception {
		ResourceLookup resourceLookup = moduleRegistry.getResourceLookup();

		Assert.assertFalse(resourceLookup.getResourceClasses().contains(Object.class));
		Assert.assertFalse(resourceLookup.getResourceClasses().contains(String.class));
		Assert.assertTrue(resourceLookup.getResourceClasses().contains(TestResource.class));

		Assert.assertFalse(resourceLookup.getResourceRepositoryClasses().contains(Object.class));
		Assert.assertFalse(resourceLookup.getResourceRepositoryClasses().contains(String.class));
		Assert.assertTrue(resourceLookup.getResourceRepositoryClasses().contains(TestRepository.class));
	}

	@Test
	public void testJacksonModule() throws Exception {
		List<com.fasterxml.jackson.databind.Module> jacksonModules = moduleRegistry.getJacksonModules();
		Assert.assertEquals(1, jacksonModules.size());
		com.fasterxml.jackson.databind.Module jacksonModule = jacksonModules.get(0);
		Assert.assertEquals("test", jacksonModule.getModuleName());
	}

	@Test
	public void testFilter() throws Exception {
		List<Filter> filters = moduleRegistry.getFilters();
		Assert.assertEquals(1, filters.size());
	}

	@Test
	public void testRepositoryRegistration() {
		RegistryEntry<?> entry = resourceRegistry.getEntry(TestResource2.class);
		ResourceInformation info = entry.getResourceInformation();
		Assert.assertEquals(TestResource2.class, info.getResourceClass());

		Assert.assertNotNull(entry.getResourceRepository(null));
		List<?> relationshipEntries = entry.getRelationshipEntries();
		Assert.assertEquals(1, relationshipEntries.size());
		DirectResponseRelationshipEntry<?, ?> responseRelationshipEntry = (DirectResponseRelationshipEntry<?, ?>) relationshipEntries
				.get(0);
		Assert.assertNotNull(responseRelationshipEntry);
	}

	class TestModule implements Module {

		@Override
		public String getModuleName() {
			return "test";
		}

		@Override
		public void setupModule(ModuleContext context) {
			context.addResourceLookup(new TestResourceLookup());
			context.addResourceInformationBuilder(new TestResourceInformationBuilder());

			context.addJacksonModule(new com.fasterxml.jackson.databind.module.SimpleModule() {
				private static final long serialVersionUID = 7829254359521781942L;

				@Override
				public String getModuleName() {
					return "test";
				}
			});

			context.addFilter(new TestFilter());
			context.addRepository(TestResource2.class, new TestRepository2());
			context.addRepository(TestResource2.class, TestResource2.class, new TestRelationshipRepository2());

		}
	}

	@JsonApiResource(type = "test2")
	static class TestResource2 {

		@JsonApiId
		private int id;

		private TestResource2 parent;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public TestResource2 getParent() {
			return parent;
		}

		public void setParent(TestResource2 parent) {
			this.parent = parent;
		}
	}

	class TestRelationshipRepository2
			implements RelationshipRepository<TestResource2, Integer, TestResource2, Integer> {

		@Override
		public void setRelation(TestResource2 source, Integer targetId, String fieldName) {
		}

		@Override
		public void setRelations(TestResource2 source, Iterable<Integer> targetIds, String fieldName) {
		}

		@Override
		public void addRelations(TestResource2 source, Iterable<Integer> targetIds, String fieldName) {
		}

		@Override
		public void removeRelations(TestResource2 source, Iterable<Integer> targetIds, String fieldName) {
		}

		@Override
		public TestResource2 findOneTarget(Integer sourceId, String fieldName, QueryParams queryParams) {
			return null;
		}

		@Override
		public Iterable<TestResource2> findManyTargets(Integer sourceId, String fieldName, QueryParams queryParams) {
			return null;
		}
	}

	class TestRepository2 extends TestRepository {
	}
}
