package io.katharsis.resource.registry;

import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.repository.RepositoryNotFoundException;
import io.katharsis.resource.ResourceInformationBuilder;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.repository.TaskRepository;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static io.katharsis.resource.registry.ResourceRegistryTest.TEST_MODELS_URL;

public class ResourceRegistryBuilderTest {

    public static final String TEST_MODELS_PACKAGE = "io.katharsis.resource.mock";


    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void onValidPackageShouldBuildRegistry() {
        // GIVEN
        ResourceRegistryBuilder sut = new ResourceRegistryBuilder(new SampleJsonServiceLocator(), new ResourceInformationBuilder());

        // WHEN
        ResourceRegistry resourceRegistry = sut.build(TEST_MODELS_PACKAGE, TEST_MODELS_URL);

        // THEN
        RegistryEntry tasksEntry = resourceRegistry.getEntry("tasks");
        Assert.assertNotNull(tasksEntry);
        Assert.assertEquals("id", tasksEntry.getResourceInformation().getIdField().getName());
        Assert.assertNotNull(tasksEntry.getResourceRepository());
        List tasksRelationshipRepositories = tasksEntry.getRelationshipRepositories();
        Assert.assertEquals(1, tasksRelationshipRepositories.size());
        Assert.assertEquals(TEST_MODELS_URL + "/tasks", resourceRegistry.getResourceUrl(Task.class));

        RegistryEntry projectsEntry = resourceRegistry.getEntry("projects");
        Assert.assertNotNull(projectsEntry);
        Assert.assertEquals("id", projectsEntry.getResourceInformation().getIdField().getName());
        Assert.assertNotNull(tasksEntry.getResourceRepository());
        List ProjectRelationshipRepositories = projectsEntry.getRelationshipRepositories();
        Assert.assertEquals(0, ProjectRelationshipRepositories.size());
        Assert.assertEquals(TEST_MODELS_URL + "/projects", resourceRegistry.getResourceUrl(Project.class));
    }

    @Test
    public void onValidPackagesShouldBuildRegistry() {
        // GIVEN
        ResourceRegistryBuilder sut = new ResourceRegistryBuilder(new SampleJsonServiceLocator(), new ResourceInformationBuilder());
        String packageNames = String.format("java.lang,%s,io.katharsis.locator", TEST_MODELS_PACKAGE);

        // WHEN
        ResourceRegistry resourceRegistry = sut.build(packageNames, TEST_MODELS_URL);

        // THEN
        RegistryEntry tasksEntry = resourceRegistry.getEntry("tasks");
        Assert.assertNotNull(tasksEntry);
    }

    @Test
    public void onNoEntityRepositoryInstanceShouldThrowException() {
        // GIVEN
        ResourceRegistryBuilder sut = new ResourceRegistryBuilder(new SampleJsonServiceLocator() {
            public <T> T getInstance(Class<T> clazz) {
                if (clazz == TaskRepository.class) {
                    return null;
                } else {
                    return super.getInstance(clazz);
                }
            }
        }, new ResourceInformationBuilder());

        // THEN
        expectedException.expect(RepositoryNotFoundException.class);

        // WHEN
        sut.build(TEST_MODELS_PACKAGE, TEST_MODELS_URL);
    }

    @Test
    public void onNoRelationshipRepositoryInstanceShouldThrowException() {
        // GIVEN
        ResourceRegistryBuilder sut = new ResourceRegistryBuilder(new SampleJsonServiceLocator() {
            public <T> T getInstance(Class<T> clazz) {
                if (clazz == TaskToProjectRepository.class) {
                    return null;
                } else {
                    return super.getInstance(clazz);
                }
            }
        }, new ResourceInformationBuilder());

        // THEN
        expectedException.expect(RepositoryNotFoundException.class);

        // WHEN
        sut.build(TEST_MODELS_PACKAGE, TEST_MODELS_URL);
    }

    @Test
    public void onNoRepositoryShouldThrowException() {
        // GIVEN
        ResourceRegistryBuilder sut = new ResourceRegistryBuilder(new SampleJsonServiceLocator() {
            public <T> T getInstance(Class<T> clazz) {
                if (clazz == TaskToProjectRepository.class) {
                    return null;
                } else {
                    return super.getInstance(clazz);
                }
            }
        }, new ResourceInformationBuilder());

        // THEN
        expectedException.expect(RepositoryNotFoundException.class);

        // WHEN
        sut.build(TEST_MODELS_PACKAGE, TEST_MODELS_URL);
    }
}
