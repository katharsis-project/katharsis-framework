package io.katharsis.resource.registry;

import io.katharsis.context.SampleJsonApplicationContext;
import io.katharsis.repository.RepositoryNotFoundException;
import io.katharsis.resource.mock.repository.TaskRepository;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

public class ResourceRegistryBuilderTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void onValidPackageShouldBuildRegistry() {
        // GIVEN
        String packageName = "io.katharsis.resource.mock";
        ResourceRegistryBuilder sut = new ResourceRegistryBuilder(new SampleJsonApplicationContext());

        // WHEN
        ResourceRegistry resourceRegistry = sut.build(packageName);

        // THEN
        RegistryEntry tasksEntry = resourceRegistry.getEntry("tasks");
        Assert.assertNotNull(tasksEntry.getEntityRepository());
        List tasksRelationshipRepositories = tasksEntry.getRelationshipRepositories();
        Assert.assertEquals(1, tasksRelationshipRepositories.size());

        RegistryEntry projectsEntry = resourceRegistry.getEntry("projects");
        Assert.assertNotNull(tasksEntry.getEntityRepository());
        List ProjectRelationshipRepositories = projectsEntry.getRelationshipRepositories();
        Assert.assertEquals(0, ProjectRelationshipRepositories.size());
    }

    @Test
    public void onNoEntityRepositoryInstanceShouldThrowException() {
        // GIVEN
        String packageName = "io.katharsis.resource.mock";
        ResourceRegistryBuilder sut = new ResourceRegistryBuilder(new SampleJsonApplicationContext(){
            public <T> T getInstance(Class<T> clazz) {
                if (clazz == TaskRepository.class) {
                    return null;
                } else {
                    return super.getInstance(clazz);
                }
            }
        });

        // THEN
        expectedException.expect(RepositoryNotFoundException.class);

        // WHEN
        sut.build(packageName);
    }

    @Test
    public void onNoRelationshipRepositoryInstanceShouldThrowException() {
        // GIVEN
        String packageName = "io.katharsis.resource.mock";
        ResourceRegistryBuilder sut = new ResourceRegistryBuilder(new SampleJsonApplicationContext(){
            public <T> T getInstance(Class<T> clazz) {
                if (clazz == TaskToProjectRepository.class) {
                    return null;
                } else {
                    return super.getInstance(clazz);
                }
            }
        });

        // THEN
        expectedException.expect(RepositoryNotFoundException.class);

        // WHEN
        sut.build(packageName);
    }

    @Test
    public void onNoRepositoryShouldThrowException() {
        // GIVEN
        String packageName = "io.katharsis.resource.mock.models";
        ResourceRegistryBuilder sut = new ResourceRegistryBuilder(new SampleJsonApplicationContext(){
            public <T> T getInstance(Class<T> clazz) {
                if (clazz == TaskToProjectRepository.class) {
                    return null;
                } else {
                    return super.getInstance(clazz);
                }
            }
        });

        // THEN
        expectedException.expect(RepositoryNotFoundException.class);

        // WHEN
        sut.build(packageName);
    }
}
