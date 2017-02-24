package io.katharsis.resource.registry;

import static io.katharsis.resource.registry.ResourceRegistryTest.TEST_MODELS_URL;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.katharsis.core.internal.repository.adapter.RelationshipRepositoryAdapter;
import io.katharsis.core.internal.repository.adapter.ResourceRepositoryAdapter;
import io.katharsis.core.internal.resource.AnnotationResourceInformationBuilder;
import io.katharsis.errorhandling.exception.RepositoryInstanceNotFoundException;
import io.katharsis.legacy.locator.SampleJsonServiceLocator;
import io.katharsis.legacy.registry.ResourceRegistryBuilder;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.resource.information.ResourceFieldNameTransformer;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.mock.models.Document;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.ResourceWithoutRepository;
import io.katharsis.resource.mock.models.Thing;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;

public class ResourceRegistryBuilderTest {

	public static final String TEST_MODELS_PACKAGE = "io.katharsis.resource.mock";
	private ResourceInformationBuilder resourceInformationBuilder;

	private ModuleRegistry moduleRegistry = new ModuleRegistry();

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		resourceInformationBuilder = new AnnotationResourceInformationBuilder(new ResourceFieldNameTransformer());
	}

	@Test
	public void onValidPackageShouldBuildRegistry() {
		// GIVEN
		ResourceRegistryBuilder sut = new ResourceRegistryBuilder(moduleRegistry, new SampleJsonServiceLocator(), resourceInformationBuilder);

		// WHEN
		ResourceRegistry resourceRegistry = sut.build(TEST_MODELS_PACKAGE, new ModuleRegistry(), new ConstantServiceUrlProvider(TEST_MODELS_URL));

		// THEN
		RegistryEntry tasksEntry = resourceRegistry.getEntry("tasks");
		Assert.assertNotNull(tasksEntry);
		Assert.assertEquals("id", tasksEntry.getResourceInformation().getIdField().getUnderlyingName());
		Assert.assertNotNull(tasksEntry.getResourceRepository(null));
		List tasksRelationshipRepositories = tasksEntry.getRelationshipEntries();
		Assert.assertEquals(1, tasksRelationshipRepositories.size());
		Assert.assertEquals(TEST_MODELS_URL + "/tasks", resourceRegistry.getResourceUrl(tasksEntry.getResourceInformation()));

		RegistryEntry projectsEntry = resourceRegistry.getEntry("projects");
		Assert.assertNotNull(projectsEntry);
		Assert.assertEquals("id", projectsEntry.getResourceInformation().getIdField().getUnderlyingName());
		Assert.assertNotNull(tasksEntry.getResourceRepository(null));
		List ProjectRelationshipRepositories = projectsEntry.getRelationshipEntries();
		Assert.assertEquals(2, ProjectRelationshipRepositories.size());
		Assert.assertEquals(TEST_MODELS_URL + "/projects", resourceRegistry.getResourceUrl(projectsEntry.getResourceInformation()));
	}

	@Test
	public void onValidPackagesShouldBuildRegistry() {
		// GIVEN
		ResourceRegistryBuilder sut = new ResourceRegistryBuilder(moduleRegistry, new SampleJsonServiceLocator(), resourceInformationBuilder);
		String packageNames = String.format("java.lang,%s,io.katharsis.locator", TEST_MODELS_PACKAGE);

		// WHEN
		ResourceRegistry resourceRegistry = sut.build(packageNames, new ModuleRegistry(), new ConstantServiceUrlProvider(TEST_MODELS_URL));

		// THEN
		RegistryEntry tasksEntry = resourceRegistry.getEntry("tasks");
		Assert.assertNotNull(tasksEntry);
	}

	@Test
	public void onNoRelationshipRepositoryInstanceShouldThrowException() {
		// GIVEN
		ResourceRegistryBuilder sut = new ResourceRegistryBuilder(moduleRegistry, new SampleJsonServiceLocator() {
			public <T> T getInstance(Class<T> clazz) {
				if (clazz == TaskToProjectRepository.class) {
					return null;
				} else {
					return super.getInstance(clazz);
				}
			}
		}, resourceInformationBuilder);

		// THEN
		expectedException.expect(RepositoryInstanceNotFoundException.class);

		// WHEN
		sut.build(TEST_MODELS_PACKAGE, new ModuleRegistry(), new ConstantServiceUrlProvider(TEST_MODELS_URL));
	}

	@Test
	public void onNoRepositoryShouldCreateNotFoundRepository() {
		// GIVEN
		ResourceRegistryBuilder sut = new ResourceRegistryBuilder(moduleRegistry, new SampleJsonServiceLocator(), resourceInformationBuilder);

		// WHEN
		ResourceRegistry result = sut.build(TEST_MODELS_PACKAGE, new ModuleRegistry(), new ConstantServiceUrlProvider(TEST_MODELS_URL));

		// THEN
		RegistryEntry entry = result.findEntry(ResourceWithoutRepository.class);

		assertThat(entry.getResourceInformation().getResourceClass()).isEqualTo(ResourceWithoutRepository.class);
		assertThat(entry.getResourceRepository(null)).isExactlyInstanceOf(ResourceRepositoryAdapter.class);
		assertThat(entry.getRelationshipRepositoryForClass(Project.class, null)).isExactlyInstanceOf(RelationshipRepositoryAdapter.class);
	}

	@Test
	public void onInheritedResourcesShouldAddInformationToEntry() {
		// GIVEN
		ResourceRegistryBuilder sut = new ResourceRegistryBuilder(moduleRegistry, new SampleJsonServiceLocator(), resourceInformationBuilder);
		String packageNames = String.format("java.lang,%s,io.katharsis.locator", TEST_MODELS_PACKAGE);

		// WHEN
		ResourceRegistry resourceRegistry = sut.build(packageNames, moduleRegistry, new ConstantServiceUrlProvider(TEST_MODELS_URL));

		// THEN
		RegistryEntry memorandaEntry = resourceRegistry.getEntry("memoranda");
		assertThat(memorandaEntry.getParentRegistryEntry()).isNotNull();
		assertThat(memorandaEntry.getParentRegistryEntry().getResourceInformation().getResourceClass()).isEqualTo(Document.class);
		assertThat(memorandaEntry.getParentRegistryEntry().getParentRegistryEntry()).isNotNull();
		assertThat(memorandaEntry.getParentRegistryEntry().getParentRegistryEntry().getResourceInformation().getResourceClass()).isEqualTo(Thing.class);
	}

	@Test
	public void onNonInheritedResourcesShouldNotAddInformationToEntry() {
		// GIVEN
		ResourceRegistryBuilder sut = new ResourceRegistryBuilder(moduleRegistry, new SampleJsonServiceLocator(), resourceInformationBuilder);

		// WHEN
		ResourceRegistry resourceRegistry = sut.build(TEST_MODELS_PACKAGE, new ModuleRegistry(), new ConstantServiceUrlProvider(TEST_MODELS_URL));

		// THEN
		RegistryEntry tasksEntry = resourceRegistry.getEntry("tasks");
		assertThat(tasksEntry.getParentRegistryEntry()).isNull();
	}
}
