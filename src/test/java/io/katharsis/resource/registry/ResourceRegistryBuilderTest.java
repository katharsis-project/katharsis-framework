package io.katharsis.resource.registry;

import io.katharsis.context.SampleJsonApplicationContext;
import io.katharsis.repository.RepositoryNotFoundException;
import io.katharsis.resource.ResourceInformationBuilder;
import io.katharsis.resource.mock.repository.TaskRepository;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

public class ResourceRegistryBuilderTest {

    public static final String TEST_MODELS_PACKAGE = "io.katharsis.resource.mock";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void onValidPackageShouldBuildRegistry() {
        // GIVEN
        ResourceRegistryBuilder sut = new ResourceRegistryBuilder(new SampleJsonApplicationContext(), new ResourceInformationBuilder());

        // WHEN
        ResourceRegistry resourceRegistry = sut.build(TEST_MODELS_PACKAGE);

        // THEN
        RegistryEntry tasksEntry = resourceRegistry.getEntry("tasks");
        Assert.assertEquals("id", tasksEntry.getResourceInformation().getIdField().getName());
        Assert.assertNotNull(tasksEntry.getEntityRepository());
        List tasksRelationshipRepositories = tasksEntry.getRelationshipRepositories();
        Assert.assertEquals(1, tasksRelationshipRepositories.size());

        RegistryEntry projectsEntry = resourceRegistry.getEntry("projects");
        Assert.assertEquals("id", projectsEntry.getResourceInformation().getIdField().getName());
        Assert.assertNotNull(tasksEntry.getEntityRepository());
        List ProjectRelationshipRepositories = projectsEntry.getRelationshipRepositories();
        Assert.assertEquals(0, ProjectRelationshipRepositories.size());
    }

    @Test
    public void onNoEntityRepositoryInstanceShouldThrowException() {
        // GIVEN
        ResourceRegistryBuilder sut = new ResourceRegistryBuilder(new SampleJsonApplicationContext() {
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
        sut.build(TEST_MODELS_PACKAGE);
    }

    @Test
    public void onNoRelationshipRepositoryInstanceShouldThrowException() {
        // GIVEN
        ResourceRegistryBuilder sut = new ResourceRegistryBuilder(new SampleJsonApplicationContext() {
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
        sut.build(TEST_MODELS_PACKAGE);
    }

    @Test
    public void onNoRepositoryShouldThrowException() {
        // GIVEN
        ResourceRegistryBuilder sut = new ResourceRegistryBuilder(new SampleJsonApplicationContext() {
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
        sut.build(TEST_MODELS_PACKAGE);
    }
}
